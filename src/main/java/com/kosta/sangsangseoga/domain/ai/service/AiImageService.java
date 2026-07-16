package com.kosta.sangsangseoga.domain.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kosta.sangsangseoga.domain.ai.dto.AiGenerateImageRequestDto;
import com.kosta.sangsangseoga.domain.ai.dto.AiGenerateImageResponseDto;
import com.kosta.sangsangseoga.domain.ai.entity.AiGenerationUsage;
import com.kosta.sangsangseoga.domain.ai.enums.CallType;
import com.kosta.sangsangseoga.domain.ai.repository.AiGenerationUsageRepository;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.sql.DataSource;

/**
 * Python이 돌려주는 imageUrl은 Replicate의 임시 delivery URL이라, 여기서 받은 직후
 * AiImageStorageService로 즉시 로컬에 내려받아 영구 URL로 바꿔치기한 뒤 응답/기록에 쓴다
 * (Replicate URL은 DB/응답 어디에도 남기지 않는다).
 * book_image 저장, book.cover_image_id 갱신은 여전히 이번 범위 밖이다
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
    private final ObjectMapper objectMapper;
    private final DataSource dataSource;
    private final AiImageStorageService aiImageStorageService;

    private final RestTemplate restTemplate = new RestTemplate();

    public AiGenerateImageResponseDto generateImage(AiGenerateImageRequestDto request, Long memberId, String requestId) {
        long methodStartNs = System.nanoTime();
        String taskType = "GENERATE_IMAGE:" + request.getImageType();
        boolean success = false;
        int httpStatus = 0;
        String errorType = null;
        long pythonCallMs = 0L;
        long usageRecordMs = 0L;
        int reqLen = jsonLength(request);
        int resLen = 0;

        try {
            String url = fastApiBaseUrl + "/api/ai/generate-image";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Request-ID", requestId);

            log.debug("Python 이미지 생성 요청 URL: {}", url);
            log.debug("Python 이미지 생성 요청 Body: {}", request);

            logHikariStatus("beforePythonCall", requestId);

            AiGenerateImageResponseDto response;
            long t1 = System.nanoTime();
            try {
                ResponseEntity<AiGenerateImageResponseDto> result = restTemplate.postForEntity(
                        url, new HttpEntity<>(request, headers), AiGenerateImageResponseDto.class);
                pythonCallMs = elapsedMs(t1);
                httpStatus = result.getStatusCodeValue();
                response = result.getBody();
                log.debug("Python 이미지 생성 응답 Body: {}", response);
            } catch (HttpClientErrorException e) {
                // Python이 400을 반환하는 경우(imageType=PAGE인데 pageNo 누락, 지원하지 않는 style 등)는
                // 요청 자체의 문제이므로 그대로 BAD_REQUEST로 전달한다.
                pythonCallMs = elapsedMs(t1);
                httpStatus = e.getStatusCode().value();
                errorType = e.getClass().getSimpleName();
                log.warn("Python 이미지 생성 요청이 거부되었습니다: {}", e.getResponseBodyAsString());
                throw new CustomException(CommonErrorCode.BAD_REQUEST);
            } catch (RestClientException e) {
                pythonCallMs = elapsedMs(t1);
                errorType = e.getClass().getSimpleName();
                log.error("이미지 생성 처리 중 오류 발생", e);
                throw new CustomException(CommonErrorCode.INTERNAL_SERVER_ERROR);
            }

            logHikariStatus("afterPythonCall", requestId);
            resLen = jsonLength(response);

            if (response != null && response.isSuccess()) {
                String replicateUrl = response.getImageUrl();
                if (replicateUrl == null || replicateUrl.isBlank()) {
                    // Python이 success=true인데 imageUrl이 없는 건 응답 계약 위반이다.
                    // Replicate URL도 없는 상태로 "성공"만 돌려주면 화면에 이미지가 안 뜨는데 원인도
                    // 안 남으니, 여기서 명시적으로 실패 처리한다.
                    errorType = "MissingImageUrl";
                    throw new CustomException(CommonErrorCode.IMAGE_SAVE_FAILED);
                }

                long tSave = System.nanoTime();
                String localRelativeUrl = aiImageStorageService.downloadAndStore(replicateUrl, request.getImageType());
                long saveMs = elapsedMs(tSave);
                log.info("[AI-PERF] requestId={} imageSaveMs={}", requestId, saveMs);

                String localAbsoluteUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path(localRelativeUrl)
                        .toUriString();
                response.setImageUrl(localAbsoluteUrl);
                response.setMessage("이미지 생성 및 로컬 저장 완료");

                long t2 = System.nanoTime();
                recordUsage(memberId);
                usageRecordMs = elapsedMs(t2);
                logHikariStatus("afterUsageRecord", requestId);
            }

            success = response != null && response.isSuccess();
            return response;
        } catch (CustomException e) {
            if (errorType == null) {
                errorType = e.getClass().getSimpleName();
            }
            throw e;
        } finally {
            long springTotalMs = elapsedMs(methodStartNs);
            long springProcessingMs = springTotalMs - pythonCallMs;
            log.info("[AI-PERF] requestId={} taskType={} success={} httpStatus={} errorType={} reqLen={} resLen={} "
                            + "mappingMs=0 pythonCallMs={} usageRecordMs={} springProcessingMs={} springTotalMs={}",
                    requestId, taskType, success, httpStatus, errorType == null ? "" : errorType,
                    reqLen, resLen, pythonCallMs, usageRecordMs, springProcessingMs, springTotalMs);
        }
    }

    private long elapsedMs(long startNs) {
        return (System.nanoTime() - startNs) / 1_000_000;
    }

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

    private int jsonLength(Object value) {
        try {
            return objectMapper.writeValueAsString(value).length();
        } catch (Exception e) {
            return 0;
        }
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
