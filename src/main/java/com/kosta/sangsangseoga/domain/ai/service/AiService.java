package com.kosta.sangsangseoga.domain.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kosta.sangsangseoga.domain.ai.dto.AiGenerateRequestDto;
import com.kosta.sangsangseoga.domain.ai.dto.AiGenerateResponseDto;
import com.kosta.sangsangseoga.domain.ai.entity.AiGenerationUsage;
import com.kosta.sangsangseoga.domain.ai.enums.CallType;
import com.kosta.sangsangseoga.domain.ai.repository.AiGenerationUsageRepository;
import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.book.repository.BookRepository;
import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.util.Map;

/**
 * 쿼터 차감(consumeText/canGenerateFreeTrialText 등)은 이번 범위에 포함하지 않는다.
 * 여기서는 관리자 AI 사용량 대시보드가 쓸 수 있도록 호출 이력(AiGenerationUsage)만 기록한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AiService {

	@Value("${fastapi.base-url}")
	private String fastApiBaseUrl;

	private final AiPythonRequestMapper aiPythonRequestMapper;
	private final ObjectMapper objectMapper;
	private final AiGenerationUsageRepository aiGenerationUsageRepository;
	private final MemberRepository memberRepository;
	private final BookRepository bookRepository;
	private final DataSource dataSource;

	private final RestTemplate restTemplate = new RestTemplate();

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
				ResponseEntity<Map> response = restTemplate.postForEntity(
						url, new HttpEntity<>(pythonRequestBody, headers), Map.class);
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
			recordUsage(memberId, request, pythonRequestBody, fastApiResponse);
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

	/**
	 * @Transactional 메서드가 Python 응답 대기 중에도 JDBC 커넥션을 점유하는지는 코드만 봐서는
	 * 단정할 수 없다(Hibernate의 connection handling mode에 따라 lazy acquisition일 수 있음).
	 * 그래서 실제 HikariCP pool 지표를 단계별로 찍어 실측한다.
	 */
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
	 * 없는 경우(구버전 FastAPI 등)에는 요청/응답을 JSON으로 직렬화한 문자 길이를 근사치로 대신 쓴다.
	 */
	private void recordUsage(Long memberId, AiGenerateRequestDto request,
			Map<String, Object> pythonRequestBody, Map<String, Object> fastApiResponse) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));
		Book book = request.getBookId() != null ? bookRepository.findById(request.getBookId()).orElse(null) : null;

		Map<String, Object> usage = asMap(fastApiResponse == null ? null : fastApiResponse.get("usage"));
		Integer inputTokenCount = toInteger(usage.get("inputTokenCount"));
		Integer outputTokenCount = toInteger(usage.get("outputTokenCount"));

		aiGenerationUsageRepository.save(AiGenerationUsage.builder()
				.member(member)
				.book(book)
				.callType(CallType.TEXT)
				.inputTokenCount(inputTokenCount != null ? inputTokenCount : jsonLength(pythonRequestBody))
				.outputTokenCount(outputTokenCount != null ? outputTokenCount : jsonLength(fastApiResponse))
				.build());
	}

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
