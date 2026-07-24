package com.kosta.sangsangseoga.domain.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kosta.sangsangseoga.domain.ai.dto.AiGenerateRequestDto;
import com.kosta.sangsangseoga.domain.ai.dto.AiGenerateResponseDto;
import com.kosta.sangsangseoga.domain.subscription.service.UsageService;
import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.Map;

/**
 * Python 호출 전에 UsageService.assertCanGenerateText로 쿼터가 남아있는지 먼저 확인해 어차피 거절될
 * 요청이 Python 원가를 쓰지 않게 막고, 호출과 로컬 처리가 모두 끝난 뒤 consumeTextAndRecordUsage로
 * 실제 차감과 AiGenerationUsage 기록을 한 번에 처리한다. 이 이력은 관리자 대시보드 집계용이면서
 * 동시에 FREE 회원의 생애 호출 횟수 집계 기준이기도 하다(UsageService.canGenerateFreeTrialText 참고).
 *
 * 이 클래스는 의도적으로 @Transactional을 붙이지 않는다. Python 동기 호출(수 초~수십 초)이 하나의
 * 물리 트랜잭션 안에 들어가면 그 시간 내내 member row 락과 JDBC 커넥션을 쥐고 있게 되어, 같은 회원을
 * 건드리는 다른 요청(구독 변경, 책 발행 등)이 Lock wait timeout(1205)에 걸린다. 그래서 DB 쓰기가
 * 필요한 각 단계(assertCanGenerateText, consumeTextAndRecordUsage)를 UsageService 쪽의 짧은
 * 트랜잭션으로 각각 분리하고, Python 호출은 트랜잭션 밖에서 수행한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

	@Value("${fastapi.base-url}")
	private String fastApiBaseUrl;

	@Value("${fastapi.connect-timeout-ms:5000}")
	private int connectTimeoutMs;

	@Value("${fastapi.read-timeout-ms:120000}")
	private int readTimeoutMs;

	private final AiPythonRequestMapper aiPythonRequestMapper;
	private final ObjectMapper objectMapper;
	private final DataSource dataSource;
	private final UsageService usageService;
	private final PythonCallRetrySupport pythonCallRetrySupport;

	private RestTemplate restTemplate;

	@PostConstruct
	private void initRestTemplate() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(connectTimeoutMs);
		factory.setReadTimeout(readTimeoutMs);
		this.restTemplate = new RestTemplate(factory);
	}

	public AiGenerateResponseDto generate(AiGenerateRequestDto request, Long memberId, String requestId) {
		long methodStartNs = System.nanoTime();
		String taskType = request.getStage();
		boolean success = false;
		int httpStatus = 0;
		String errorType = null;
		long mappingMs = 0L;
		long pythonCallMs = 0L;
		long usageRecordMs = 0L;
		int reqLen = 0;
		int resLen = 0;

		try {
			usageService.assertCanGenerateText(memberId);

			String url = fastApiBaseUrl + "/api/ai/generate";

			long t0 = System.nanoTime();
			Map<String, Object> pythonRequestBody = aiPythonRequestMapper.buildPythonRequestBody(request);
			mappingMs = elapsedMs(t0);
			reqLen = jsonLength(pythonRequestBody);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("X-Request-ID", requestId);

			log.debug("Python AI 요청: requestId={} taskType={} url={} reqLen={}", requestId, taskType, url, reqLen);

			logHikariStatus("beforePythonCall", requestId);

			Map<String, Object> fastApiResponse;
			long t1 = System.nanoTime();
			try {
				ResponseEntity<Map> response = pythonCallRetrySupport.postForEntityWithRetry(
						restTemplate, url, new HttpEntity<>(pythonRequestBody, headers), Map.class, requestId);
				pythonCallMs = elapsedMs(t1);
				httpStatus = response.getStatusCodeValue();
				fastApiResponse = response.getBody();
				log.debug("Python AI 응답: requestId={} httpStatus={} resLen={}",
						requestId, httpStatus, jsonLength(fastApiResponse));
			} catch (RestClientException e) {
				pythonCallMs = elapsedMs(t1);
				errorType = e.getClass().getSimpleName();
				log.error("AI 생성 처리 중 실제 오류 발생", e);
				throw new CustomException(CommonErrorCode.INTERNAL_SERVER_ERROR);
			}

			logHikariStatus("afterPythonCall", requestId);
			resLen = jsonLength(fastApiResponse);

			long t2 = System.nanoTime();
			Map<String, Object> usage = asMap(fastApiResponse == null ? null : fastApiResponse.get("usage"));
			Integer inputTokenCount = toInteger(usage.get("inputTokenCount"));
			Integer outputTokenCount = toInteger(usage.get("outputTokenCount"));
			usageService.consumeTextAndRecordUsage(
					memberId,
					request.getBookId(),
					inputTokenCount != null ? inputTokenCount : jsonLength(pythonRequestBody),
					outputTokenCount != null ? outputTokenCount : jsonLength(fastApiResponse));
			usageRecordMs = elapsedMs(t2);

			logHikariStatus("afterUsageRecord", requestId);

			success = true;
			return AiGenerateResponseDto.builder()
					.bookId(request.getBookId())
					.stage(request.getStage())
					.result(fastApiResponse)
					.build();
		} catch (CustomException e) {
			if (errorType == null) {
				errorType = e.getClass().getSimpleName();
			}
			throw e;
		} catch (Exception e) {
			errorType = e.getClass().getSimpleName();
			log.error("AI 생성 처리 중 실제 오류 발생", e);
			throw new CustomException(CommonErrorCode.INTERNAL_SERVER_ERROR);
		} finally {
			long springTotalMs = elapsedMs(methodStartNs);
			long springProcessingMs = springTotalMs - pythonCallMs;
			log.info("[AI-PERF] requestId={} taskType={} success={} httpStatus={} errorType={} reqLen={} resLen={} "
							+ "mappingMs={} pythonCallMs={} usageRecordMs={} springProcessingMs={} springTotalMs={}",
					requestId, taskType, success, httpStatus, errorType == null ? "" : errorType,
					reqLen, resLen, mappingMs, pythonCallMs, usageRecordMs, springProcessingMs, springTotalMs);
		}
	}

	private long elapsedMs(long startNs) {
		return (System.nanoTime() - startNs) / 1_000_000;
	}

	/** Python 호출 전후로 HikariCP 풀 상태를 찍어, 커넥션이 실제로 오래 점유되지 않는지 실측한다. */
	private void logHikariStatus(String phase, String requestId) {
		if (dataSource instanceof HikariDataSource) {
			HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
			HikariPoolMXBean pool = hikariDataSource.getHikariPoolMXBean();
			if (pool != null) {
				log.info("[AI-PERF-HIKARI] requestId={} phase={} active={} idle={} total={} awaiting={}",
						requestId, phase, pool.getActiveConnections(), pool.getIdleConnections(),
						pool.getTotalConnections(), pool.getThreadsAwaitingConnection());
			}
		}
	}

	/**
	 * FastAPI 응답의 result.usage(inputTokenCount/outputTokenCount)가 실제 Gemini 토큰 수다.
	 * 없는 경우(구버전 FastAPI 등)에는 호출부에서 요청/응답을 JSON으로 직렬화한 문자 길이를 근사치로 대신 쓴다.
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> asMap(Object value) {
		return value instanceof Map ? (Map<String, Object>) value : java.util.Collections.emptyMap();
	}

	private Integer toInteger(Object value) {
		return value instanceof Number ? ((Number) value).intValue() : null;
	}

	private int jsonLength(Object value) {
		try {
			return objectMapper.writeValueAsString(value).length();
		} catch (Exception e) {
			return 0;
		}
	}
}
