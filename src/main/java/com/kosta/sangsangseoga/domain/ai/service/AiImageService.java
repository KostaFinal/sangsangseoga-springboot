package com.kosta.sangsangseoga.domain.ai.service;

import com.kosta.sangsangseoga.domain.ai.dto.AiGenerateImageRequestDto;
import com.kosta.sangsangseoga.domain.ai.dto.AiGenerateImageResponseDto;
import com.kosta.sangsangseoga.domain.ai.entity.AiGenerationUsage;
import com.kosta.sangsangseoga.domain.ai.enums.CallType;
import com.kosta.sangsangseoga.domain.ai.repository.AiGenerationUsageRepository;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * book_image 저장, S3 등 영구 스토리지 재업로드, book.cover_image_id 갱신은 여전히 이번 범위 밖이다
 * (AiGenerateImageRequestDto에 bookId가 없어 어느 책의 이미지인지도 알 수 없다).
 * 여기서는 관리자 AI 사용량 대시보드가 쓸 수 있도록 호출 이력(AiGenerationUsage, callType=IMAGE)만 기록한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AiImageService {

    @Value("${fastapi.base-url}")
    private String fastApiBaseUrl;

    private final AiGenerationUsageRepository aiGenerationUsageRepository;
    private final MemberRepository memberRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public AiGenerateImageResponseDto generateImage(AiGenerateImageRequestDto request, Long memberId) {
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

        if (response != null && response.isSuccess()) {
            recordUsage(memberId);
        }

        return response;
    }

    private void recordUsage(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        aiGenerationUsageRepository.save(AiGenerationUsage.builder()
                .member(member)
                .callType(CallType.IMAGE)
                .imageCount(1)
                .build());
    }
}
