package com.kosta.sangsangseoga.domain.ai.service;

import com.kosta.sangsangseoga.domain.ai.dto.AiGenerateRequestDto;
import com.kosta.sangsangseoga.domain.ai.dto.AiGenerateResponseDto;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AiService {

	// TODO: UsageService(subscription 도메인) 연동 필요.
	//  - generate()는 stage(taskType)와 무관하게 전부 텍스트 응답(이미지 프롬프트 포함)만 만들어서 반환하므로
	//    CallType.TEXT 사용량으로 취급하면 된다. 실제 이미지 파일 생성(Replicate 등, CallType.IMAGE)은
	//    아직 별도 연동이 없어 보인다.
	//  - 지금은 이 메서드가 회원 정보(Authentication/memberId)를 안 받는 구조라 사용량 체크가 불가능하다.
	//    호출자(컨트롤러)에서 memberId를 받아 넘기도록 시그니처를 바꿔야 아래를 걸 수 있다.
	//    - PREMIUM 회원: 호출 전 UsageService.consumeText(memberId)(잔여량 0이면 예외)
	//    - FREE 회원: UsageService.canGenerateFreeTrialText(memberId) 체크
	//      (책 페이지 수 제한과는 별개로, 재생성 남용으로 원가만 나가는 것을 막는 생애 호출 횟수 상한)

	@Value("${fastapi.base-url}")
	private String fastApiBaseUrl;

	private final AiPythonRequestMapper aiPythonRequestMapper;

	private final RestTemplate restTemplate = new RestTemplate();

	public AiGenerateResponseDto generate(AiGenerateRequestDto request) {
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

		return AiGenerateResponseDto.builder()
				.bookId(request.getBookId())
				.stage(request.getStage())
				.result(fastApiResponse)
				.build();
	}
}
