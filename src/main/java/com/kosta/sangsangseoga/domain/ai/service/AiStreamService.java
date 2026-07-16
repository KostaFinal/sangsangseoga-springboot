package com.kosta.sangsangseoga.domain.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kosta.sangsangseoga.domain.ai.dto.AiGenerateRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Python FastAPI의 POST /api/ai/generate/stream(SSE)을 그대로 중계한다.
 * RestTemplate은 응답 전체를 버퍼링하므로 스트리밍에 쓸 수 없어, 이 서비스는
 * {@link AiService}(트랜잭션 있는 non-stream 경로)와 별도로 SseEmitter + JDK HttpClient만 사용한다.
 * 새 리액티브 의존성(WebClient/WebFlux) 없이 기존 서블릿 스택 안에서 처리한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiStreamService {

	@Value("${fastapi.base-url}")
	private String fastApiBaseUrl;

	private final AiPythonRequestMapper aiPythonRequestMapper;
	private final ObjectMapper objectMapper;

	private final HttpClient httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(10))
			.build();

	private final ExecutorService executor = Executors.newFixedThreadPool(4);

	public SseEmitter streamGenerate(AiGenerateRequestDto request, String requestId) {
		long t0 = System.nanoTime();
		Map<String, Object> pythonRequestBody = aiPythonRequestMapper.buildPythonRequestBody(request);
		long mappingMs = elapsedMs(t0);
		String url = fastApiBaseUrl + "/api/ai/generate/stream";
		String taskType = request.getStage();

		log.debug("Python AI 스트리밍 요청 URL: {}", url);
		log.debug("Python AI 스트리밍 요청 Body: {}", pythonRequestBody);

		SseEmitter emitter = new SseEmitter(60_000L);
		emitter.onTimeout(emitter::complete);
		emitter.onError(e -> log.warn("SSE emitter 오류: {}", e.getMessage()));

		long submitNs = System.nanoTime();
		executor.submit(() -> proxyPythonStream(emitter, url, pythonRequestBody, requestId, taskType, mappingMs, submitNs));

		return emitter;
	}

	private void proxyPythonStream(SseEmitter emitter, String url, Map<String, Object> pythonRequestBody,
									String requestId, String taskType, long mappingMs, long submitNs) {
		boolean success = false;
		int httpStatus = 0;
		String errorType = null;
		int eventCount = 0;
		Long ttfbMs = null;
		int reqLen = 0;
		int resLen = 0;

		try {
			String requestJson = objectMapper.writeValueAsString(pythonRequestBody);
			reqLen = requestJson.length();

			HttpRequest httpRequest = HttpRequest.newBuilder()
					.uri(URI.create(url))
					.header("Content-Type", "application/json")
					.header("X-Request-ID", requestId)
					.POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
					.build();

			long sendStartNs = System.nanoTime();
			HttpResponse<InputStream> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
			httpStatus = response.statusCode();

			if (httpStatus != 200) {
				// 정상 SSE가 아닌 경우(Python이 4xx/5xx를 반환) 원인 진단을 위해 원문을 임시로 남긴다.
				String rawBody = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
				log.warn("[AI-PERF] requestId={} Python 스트리밍 응답이 200이 아님(httpStatus={}): {}",
						requestId, httpStatus, rawBody);
				errorType = "PythonNon200Response";
				emitter.send(SseEmitter.event().name("error").data("{\"message\":\"AI 서버 응답 오류\"}"));
				emitter.complete();
				return;
			}

			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {

				String eventName = null;
				String line;

				while ((line = reader.readLine()) != null) {
					if (line.startsWith("event:")) {
						eventName = line.substring("event:".length()).trim();
					} else if (line.startsWith("data:") && eventName != null) {
						if (ttfbMs == null) {
							ttfbMs = elapsedMs(sendStartNs);
						}
						String data = line.substring("data:".length()).trim();
						emitter.send(SseEmitter.event().name(eventName).data(data));
						eventCount++;

						if ("done".equals(eventName) || "error".equals(eventName)) {
							resLen = data.length();
							success = "done".equals(eventName);
							if ("error".equals(eventName)) {
								errorType = "PythonStreamError";
							}
							break;
						}
					}
				}
			}

			emitter.complete();
		} catch (Exception e) {
			errorType = e.getClass().getSimpleName();
			log.error("AI 스트리밍 처리 중 실제 오류 발생", e);
			try {
				emitter.send(SseEmitter.event().name("error").data("{\"message\":\"AI 스트리밍 처리 중 오류가 발생했습니다.\"}"));
			} catch (IOException | IllegalStateException ignored) {
				// emitter가 이미 완료/타임아웃된 경우 무시한다.
			}
			emitter.completeWithError(e);
		} finally {
			long springTotalMs = elapsedMs(submitNs);
			log.info("[AI-PERF] requestId={} taskType={} success={} httpStatus={} errorType={} reqLen={} resLen={} "
							+ "mappingMs={} eventCount={} ttfbMs={} springTotalMs={}",
					requestId, taskType, success, httpStatus, errorType == null ? "" : errorType,
					reqLen, resLen, mappingMs, eventCount, ttfbMs == null ? "" : ttfbMs, springTotalMs);
		}
	}

	private long elapsedMs(long startNs) {
		return (System.nanoTime() - startNs) / 1_000_000;
	}

	@PreDestroy
	public void shutdown() {
		executor.shutdown();
	}
}
