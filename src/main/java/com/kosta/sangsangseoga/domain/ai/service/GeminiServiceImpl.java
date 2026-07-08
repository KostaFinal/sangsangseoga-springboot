package com.kosta.sangsangseoga.domain.ai.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GeminiServiceImpl implements GeminiService {

	@Value("${gemini.api.key}")
	private String apiKey;

	@Value("${gemini.api.url}")
	private String apiUrl;

	private final RestTemplate restTemplate;

	@Override
	public String generateReviewFeedback(String bookTitle, String reviewContent)  {
		String prompt = "너는 초등학생을 위한 따뜻한 AI 독후감 선생님이야.\n" + "비판보다는 칭찬과 격려 중심으로 피드백해줘.\n"
				+ "어려운 말은 쓰지 말고, 3~5문장 정도로 작성해줘.\n\n" + "책 제목: " + bookTitle + "\n" + "독후감 내용: " + reviewContent;

		Map<String, Object> textPart = new HashMap<>();
		textPart.put("text", prompt);

		Map<String, Object> content = new HashMap<>();
		content.put("parts", Collections.singletonList(textPart));

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("contents", Collections.singletonList(content));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		String url = apiUrl + "?key=" + apiKey;

		ResponseEntity<Map> response = restTemplate.postForEntity(url, new HttpEntity<>(requestBody, headers),
				Map.class);

		return extractText(response.getBody());
	}

	private String extractText(Map responseBody) {

	    if (responseBody == null) {
	        throw new CustomException(CommonErrorCode.INTERNAL_SERVER_ERROR);
	    }

	    List candidates = (List) responseBody.get("candidates");
	    if (candidates == null || candidates.isEmpty()) {
	        throw new CustomException(CommonErrorCode.INTERNAL_SERVER_ERROR);
	    }

	    Map candidate = (Map) candidates.get(0);
	    Map content = (Map) candidate.get("content");

	    if (content == null) {
	        throw new CustomException(CommonErrorCode.INTERNAL_SERVER_ERROR);
	    }

	    List parts = (List) content.get("parts");
	    if (parts == null || parts.isEmpty()) {
	        throw new CustomException(CommonErrorCode.INTERNAL_SERVER_ERROR);
	    }

	    Map part = (Map) parts.get(0);

	    return (String) part.get("text");
	}

}
