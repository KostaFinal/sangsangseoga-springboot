package com.kosta.sangsangseoga.domain.ai.service;

import com.kosta.sangsangseoga.global.config.AppProperties;
import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;

/**
 * Replicate가 반환하는 imageUrl은 일정 시간이 지나면 만료되는 임시 delivery URL이라, DB에 그대로
 * 저장하면 나중에 이미지가 깨진다. 여기서 그 URL의 바이트를 즉시 내려받아 app.upload.image-dir
 * 아래 영구 저장하고, app.upload.image-url 기준 상대 경로(WebConfig가 정적 리소스로 서빙)를 돌려준다.
 *
 * 로컬 저장이 실패하면 Replicate URL로 대충 응답하지 않고 예외를 던져 요청 전체를 실패로 처리한다 -
 * 만료될 URL을 성공으로 감춰 돌려주면 나중에 이미지가 사라지는 문제가 그대로 재현되기 때문이다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiImageStorageService {

    private static final String DEFAULT_EXTENSION = "webp";

    private final AppProperties appProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    @Getter
    @AllArgsConstructor
    public static class StoredImage {
        // 실패 시 정리(deleteQuietly)할 수 있도록 실제 파일 경로를 들고 있는다.
        private final Path file;
        private final String relativeUrl;
    }

    public StoredImage downloadAndStore(String remoteUrl, String imageType) {
        ResponseEntity<byte[]> response;
        try {
            response = restTemplate.exchange(remoteUrl, HttpMethod.GET, null, byte[].class);
        } catch (RestClientException e) {
            log.error("Replicate 이미지 다운로드 실패: url={}", remoteUrl, e);
            throw new CustomException(CommonErrorCode.IMAGE_SAVE_FAILED);
        }

        byte[] body = response.getBody();
        if (body == null || body.length == 0) {
            log.error("Replicate 이미지 다운로드 결과가 0바이트입니다: url={}", remoteUrl);
            throw new CustomException(CommonErrorCode.IMAGE_SAVE_FAILED);
        }

        MediaType contentType = response.getHeaders().getContentType();
        if (contentType != null && !"image".equalsIgnoreCase(contentType.getType())) {
            log.error("Replicate 응답이 이미지가 아닙니다: url={}, contentType={}", remoteUrl, contentType);
            throw new CustomException(CommonErrorCode.IMAGE_SAVE_FAILED);
        }

        String extension = resolveExtension(contentType, remoteUrl);
        String subDir = "COVER".equalsIgnoreCase(imageType) ? "cover" : "page";

        Path targetDir = Paths.get(appProperties.getUpload().getImageDir(), subDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(targetDir);
        } catch (IOException e) {
            log.error("이미지 저장 디렉터리 생성 실패: dir={}", targetDir, e);
            throw new CustomException(CommonErrorCode.IMAGE_SAVE_FAILED);
        }

        String fileName = UUID.randomUUID() + "." + extension;
        Path targetFile = targetDir.resolve(fileName);

        try {
            Files.write(targetFile, body);
        } catch (IOException e) {
            log.error("이미지 파일 저장 실패: file={}", targetFile, e);
            deleteQuietly(targetFile);
            throw new CustomException(CommonErrorCode.IMAGE_SAVE_FAILED);
        }

        log.info("Replicate 이미지 로컬 저장 완료: remoteUrl={}, file={}, size={}", remoteUrl, targetFile, body.length);

        String relativeUrl = appProperties.getUpload().getImageUrl() + "/" + subDir + "/" + fileName;
        return new StoredImage(targetFile, relativeUrl);
    }

    /**
     * 다운로드+저장 자체는 성공했지만 이후 단계(예: 사용량 기록)가 실패해 요청 전체를 되돌려야 할 때,
     * 호출부(AiImageService)가 보상 삭제를 하기 위해 쓰는 공개 메서드다.
     */
    public void deleteQuietly(StoredImage storedImage) {
        if (storedImage == null) {
            return;
        }
        deleteQuietly(storedImage.getFile());
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("불완전한 이미지 파일 삭제 실패: file={}", path, e);
        }
    }

    private String resolveExtension(MediaType contentType, String url) {
        if (contentType != null) {
            String subtype = contentType.getSubtype().toLowerCase(Locale.ROOT);
            if (subtype.equals("jpeg")) return "jpg";
            if (subtype.equals("png") || subtype.equals("webp")) return subtype;
        }

        String path = url.contains("?") ? url.substring(0, url.indexOf('?')) : url;
        int dotIdx = path.lastIndexOf('.');
        if (dotIdx >= 0) {
            String ext = path.substring(dotIdx + 1).toLowerCase(Locale.ROOT);
            if (ext.equals("jpeg")) return "jpg";
            if (ext.equals("png") || ext.equals("jpg") || ext.equals("webp")) return ext;
        }

        return DEFAULT_EXTENSION;
    }
}
