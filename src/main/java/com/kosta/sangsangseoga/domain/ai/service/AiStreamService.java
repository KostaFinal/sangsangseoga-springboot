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

	public SseEmitter streamGenerate(AiGenerateRequestDto request) {
		Map<String, Object> pythonRequestBody = aiPythonRequestMapper.buildPythonRequestBody(request);
		String url = fastApiBaseUrl + "/api/ai/generate/stream";

		log.info("Python AI 스트리밍 요청 URL: {}", url);
		log.info("Python AI 스트리밍 요청 Body: {}", pythonRequestBody);

		SseEmitter emitter = new SseEmitter(60_000L);
		emitter.onTimeout(emitter::complete);
		emitter.onError(e -> log.warn("SSE emitter 오류: {}", e.getMessage()));

		executor.submit(() -> proxyPythonStream(emitter, url, pythonRequestBody));

		return emitter;
	}

	private void proxyPythonStream(SseEmitter emitter, String url, Map<String, Object> pythonRequestBody) {
		try {
			String requestJson = objectMapper.writeValueAsString(pythonRequestBody);

			HttpRequest httpRequest = HttpRequest.newBuilder()
					.uri(URI.create(url))
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
					.build();

			HttpResponse<InputStream> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());

			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {

				String eventName = null;
				String line;

				while ((line = reader.readLine()) != null) {
					if (line.startsWith("event:")) {
						eventName = line.substring("event:".length()).trim();
					} else if (line.startsWith("data:") && eventName != null) {
						String data = line.substring("data:".length()).trim();
						emitter.send(SseEmitter.event().name(eventName).data(data));

						if ("done".equals(eventName) || "error".equals(eventName)) {
							break;
						}
					}
				}
			}

			emitter.complete();
		} catch (Exception e) {
			log.error("AI 스트리밍 처리 중 실제 오류 발생", e);
			try {
				emitter.send(SseEmitter.event().name("error").data("{\"message\":\"AI 스트리밍 처리 중 오류가 발생했습니다.\"}"));
			} catch (IOException | IllegalStateException ignored) {
				// emitter가 이미 완료/타임아웃된 경우 무시한다.
			}
			emitter.completeWithError(e);
		}
	}

	@PreDestroy
	public void shutdown() {
		executor.shutdown();
	}
}
