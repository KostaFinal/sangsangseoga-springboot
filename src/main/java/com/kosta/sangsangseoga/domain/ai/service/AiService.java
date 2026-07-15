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

	private final RestTemplate restTemplate = new RestTemplate();

	public AiGenerateResponseDto generate(AiGenerateRequestDto request, Long memberId) {
		String url = fastApiBaseUrl + "/api/ai/generate";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		Map<String, Object> pythonRequestBody = aiPythonRequestMapper.buildPythonRequestBody(request);

		log.info("Python AI 요청 URL: {}", url);
		log.info("Python AI 요청 Body: {}", pythonRequestBody);

		Map<String, Object> fastApiResponse;
		try {
			ResponseEntity<Map> response = restTemplate.postForEntity(
					url, new HttpEntity<>(pythonRequestBody, headers), Map.class);
			fastApiResponse = response.getBody();
			log.info("Python AI 응답 Body: {}", fastApiResponse);
		} catch (RestClientException e) {
			log.error("AI 생성 처리 중 실제 오류 발생", e);
			throw new CustomException(CommonErrorCode.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			log.error("AI 생성 처리 중 실제 오류 발생", e);
			throw new CustomException(CommonErrorCode.INTERNAL_SERVER_ERROR);
		}

		recordUsage(memberId, request, pythonRequestBody, fastApiResponse);

		return AiGenerateResponseDto.builder()
				.bookId(request.getBookId())
				.stage(request.getStage())
				.result(fastApiResponse)
				.build();
	}

	/**
	 * Gemini 실제 토큰 수는 FastAPI 응답에 포함되지 않아 알 수 없다. 대신 요청/응답을 JSON으로
	 * 직렬화한 문자 길이를 입출력 크기의 근사치로 저장한다(정확한 토큰 수가 아님에 유의).
	 */
	private void recordUsage(Long memberId, AiGenerateRequestDto request,
							  Map<String, Object> pythonRequestBody, Map<String, Object> fastApiResponse) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));
		Book book = request.getBookId() != null ? bookRepository.findById(request.getBookId()).orElse(null) : null;

		aiGenerationUsageRepository.save(AiGenerationUsage.builder()
				.member(member)
				.book(book)
				.callType(CallType.TEXT)
				.inputTokenCount(jsonLength(pythonRequestBody))
				.outputTokenCount(jsonLength(fastApiResponse))
				.build());
	}

	private int jsonLength(Object value) {
		try {
			return objectMapper.writeValueAsString(value).length();
		} catch (Exception e) {
			return 0;
		}
	}
}
