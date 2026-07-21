package com.kosta.sangsangseoga.domain.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kosta.sangsangseoga.domain.ai.dto.AiGenerateImageRequestDto;
import com.kosta.sangsangseoga.domain.ai.dto.AiGenerateImageResponseDto;
import com.kosta.sangsangseoga.domain.ai.entity.AiGenerationUsage;
import com.kosta.sangsangseoga.domain.ai.enums.CallType;
import com.kosta.sangsangseoga.domain.ai.repository.AiGenerationUsageRepository;
import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.sql.DataSource;
import java.util.Base64;
import java.util.Optional;

/**
 * Python이 돌려주는 imageUrl은 Replicate의 임시 delivery URL이라, 여기서 받은 직후
 * AiImageStorageService로 즉시 로컬에 내려받아 영구 URL로 바꿔치기한 뒤 응답/기록에 쓴다
 * (Replicate URL은 DB/응답 어디에도 남기지 않는다).
 * book_image 저장, book.cover_image_id 갱신은 여전히 이번 범위 밖이다
 * (AiGenerateImageRequestDto에 bookId가 없어 어느 책의 이미지인지도 알 수 없다).
 * 관리자 AI 사용량 대시보드가 쓸 호출 이력(AiGenerationUsage, callType=IMAGE)을 기록하는 것과 별개로,
 * Python 호출 전에는 UsageService.assertCanGenerateImage로 쿼터를 확인하고 저장까지 끝난 뒤에는
 * consumeImage로 실제 차감한다(AiService의 텍스트 경로와 동일한 패턴).
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
    private final UsageService usageService;

    private final RestTemplate restTemplate = new RestTemplate();

    public AiGenerateImageResponseDto generateImage(AiGenerateImageRequestDto request, Long memberId, String requestId) {
        long methodStartNs = System.nanoTime();
        String taskType = "GENERATE_IMAGE:" + request.getImageType();
        boolean success = false;
        int httpStatus = 0;
        String errorType = null;
        long pythonCallMs = 0L;
        long usageRecordMs = 0L;
        int reqLen = 0;
        int resLen = 0;

        try {
            usageService.assertCanGenerateImage(memberId);

            String url = fastApiBaseUrl + "/api/ai/generate-image";

            // 캐릭터 일관성용 레퍼런스 이미지(예: 이미 생성된 표지)의 로컬 URL이 왔으면, 그 파일을
            // 읽어 base64로 채운 뒤 같은 request 객체를 그대로 Python에 넘긴다(request는 이미
            // Python의 필드명과 1:1로 맞춰 만들어진 DTO라 별도 매핑 없이 재사용한다).
            // 파일을 못 읽어도 요청 전체를 실패시키지 않고, 레퍼런스 없이(텍스트만으로) 진행한다.
            if (request.getReferenceImageUrl() != null && !request.getReferenceImageUrl().isBlank()) {
                Optional<AiImageStorageService.ReferenceImage> reference =
                        aiImageStorageService.readReferenceImage(request.getReferenceImageUrl());
                if (reference.isPresent()) {
                    request.setReferenceImageBase64(Base64.getEncoder().encodeToString(reference.get().getData()));
                    request.setReferenceImageMimeType(reference.get().getMimeType());
                } else {
                    log.warn("레퍼런스 이미지를 읽지 못해 레퍼런스 없이 진행합니다: requestId={} referenceImageUrl={}",
                            requestId, request.getReferenceImageUrl());
                }
            }

            // reqLen은 레퍼런스 base64까지 채운 뒤(=Python에 실제로 보내는 최종 페이로드 기준)에 잰다.
            reqLen = jsonLength(request);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Request-ID", requestId);

            log.debug("Python 이미지 생성 요청: requestId={} imageType={} url={} reqLen={}",
                    requestId, request.getImageType(), url, reqLen);

            logHikariStatus("beforePythonCall", requestId);

            AiGenerateImageResponseDto response;
            long t1 = System.nanoTime();
            try {
                ResponseEntity<AiGenerateImageResponseDto> result = restTemplate.postForEntity(
                        url, new HttpEntity<>(request, headers), AiGenerateImageResponseDto.class);
                pythonCallMs = elapsedMs(t1);
                httpStatus = result.getStatusCodeValue();
                response = result.getBody();
                log.debug("Python 이미지 생성 응답: requestId={} httpStatus={} resLen={}",
                        requestId, httpStatus, jsonLength(response));
            } catch (HttpClientErrorException e) {
                // Python이 400을 반환하는 경우(imageType=PAGE인데 pageNo 누락, 지원하지 않는 style 등)는
                // 요청 자체의 문제이므로 그대로 BAD_REQUEST로 전달한다.
                pythonCallMs = elapsedMs(t1);
                httpStatus = e.getStatusCode().value();
                errorType = e.getClass().getSimpleName();
                String rejectedBody = e.getResponseBodyAsString();
                log.warn("Python 이미지 생성 요청이 거부되었습니다: requestId={} httpStatus={} bodyLen={}",
                        requestId, httpStatus, rejectedBody == null ? 0 : rejectedBody.length());
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
                long tSave = System.nanoTime();
                AiImageStorageService.StoredImage storedImage;

                String imageBase64 = response.getImageBase64();
                if (imageBase64 != null && !imageBase64.isBlank()) {
                    // Gemini 전환 이후 Python의 유일한 이미지 전달 방식(data URI). Replicate 시절의
                    // imageUrl 방식은 더 이상 오지 않지만, 혹시 모를 회귀에 대비해 아래 폴백은 남겨둔다.
                    storedImage = aiImageStorageService.storeFromDataUri(imageBase64, request.getImageType());
                } else {
                    String replicateUrl = response.getImageUrl();
                    if (replicateUrl == null || replicateUrl.isBlank()) {
                        // Python이 success=true인데 imageBase64/imageUrl 둘 다 없는 건 응답 계약 위반이다.
                        // 이미지도 없이 "성공"만 돌려주면 화면에 이미지가 안 뜨는데 원인도 안 남으니,
                        // 여기서 명시적으로 실패 처리한다.
                        errorType = "MissingImageData";
                        throw new CustomException(CommonErrorCode.IMAGE_SAVE_FAILED);
                    }
                    storedImage = aiImageStorageService.downloadAndStore(replicateUrl, request.getImageType());
                }

                long saveMs = elapsedMs(tSave);
                log.info("[AI-PERF] requestId={} imageSaveMs={}", requestId, saveMs);

                String localAbsoluteUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path(storedImage.getRelativeUrl())
                        .toUriString();
                response.setImageUrl(localAbsoluteUrl);
                response.setMessage("이미지 생성 및 로컬 저장 완료");
                // 로컬 저장을 마쳤으니 응답에 굳이 몇 MB짜리 base64를 그대로 실어 보낼 필요가 없다.
                response.setImageBase64(null);

                long t2 = System.nanoTime();
                try {
                    recordUsage(memberId);
                    //usageService.consumeImage(memberId);
                } catch (RuntimeException e) {
                    // 파일 저장은 이미 끝났는데 사용량 기록/차감이 실패해 요청 전체가 실패로 되돌아가면,
                    // 그 파일은 DB 어디에도 참조되지 않는 고아 파일로 디스크에 영원히 남는다.
                    // 원래 예외는 그대로 던지되, 방금 저장한 파일은 여기서 지워 정리한다.
                    aiImageStorageService.deleteQuietly(storedImage);
                    throw e;
                }
                
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
