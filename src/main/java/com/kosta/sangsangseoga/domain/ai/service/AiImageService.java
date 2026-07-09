package com.kosta.sangsangseoga.domain.ai.service;

import com.kosta.sangsangseoga.domain.ai.dto.AiGenerateImageRequestDto;
import com.kosta.sangsangseoga.domain.ai.dto.AiGenerateImageResponseDto;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiImageService {

    @Value("${fastapi.base-url}")
    private String fastApiBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public AiGenerateImageResponseDto generateImage(AiGenerateImageRequestDto request) {
        String url = fastApiBaseUrl + "/api/ai/generate-image";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        log.info("Python 이미지 생성 요청 URL: {}", url);
        log.info("Python 이미지 생성 요청 Body: {}", request);

        AiGenerateImageResponseDto response;
        try {
            ResponseEntity<AiGenerateImageResponseDto> result = restTemplate.postForEntity(
                    url, new HttpEntity<>(request, headers), AiGenerateImageResponseDto.class);
            response = result.getBody();
            log.info("Python 이미지 생성 응답 Body: {}", response);
        } catch (HttpClientErrorException e) {
            // Python이 400을 반환하는 경우(imageType=PAGE인데 pageNo 누락, 지원하지 않는 style 등)는
            // 요청 자체의 문제이므로 그대로 BAD_REQUEST로 전달한다.
            log.warn("Python 이미지 생성 요청이 거부되었습니다: {}", e.getResponseBodyAsString());
            throw new CustomException(CommonErrorCode.BAD_REQUEST);
        } catch (RestClientException e) {
            log.error("이미지 생성 처리 중 오류 발생", e);
            throw new CustomException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }

        // TODO: book_image 저장, S3 등 영구 스토리지 재업로드, book.cover_image_id 갱신,
        // ai_generation_usage(callType=IMAGE) 기록은 후속 작업에서 이 지점에 추가한다.
        // 이번 구현 범위는 Python이 반환한 imageUrl을 그대로 돌려주는 것까지다.

        return response;
    }
}
