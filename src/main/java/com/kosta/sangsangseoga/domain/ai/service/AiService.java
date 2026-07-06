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
