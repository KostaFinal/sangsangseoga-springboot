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
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 이미지 생성 응답을 app.upload.image-dir 아래 영구 저장하고, app.upload.image-url 기준
 * 상대 경로(WebConfig가 정적 리소스로 서빙)를 돌려준다.
 *
 * Python이 Gemini로 전환된 뒤로는 imageBase64(data URI)로 이미지가 오고, imageUrl은 항상 비어 있다
 * (예전 Replicate 시절에는 반대였다 - imageUrl만 오고 imageBase64는 없었음). downloadAndStore는
 * Replicate URL 방식이 다시 쓰일 가능성에 대비해 남겨두고, storeFromDataUri를 주 경로로 쓴다.
 *
 * 로컬 저장이 실패하면 대충 응답하지 않고 예외를 던져 요청 전체를 실패로 처리한다 - 저장 안 된 이미지를
 * 성공으로 감춰 돌려주면 나중에 이미지가 안 보이는 문제가 원인도 없이 재현되기 때문이다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiImageStorageService {

    private static final String DEFAULT_EXTENSION = "webp";
    private static final Pattern DATA_URI_PATTERN = Pattern.compile("^data:([^;,]+);base64,(.+)$", Pattern.DOTALL);

    private final AppProperties appProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    @Getter
    @AllArgsConstructor
    public static class StoredImage {
        // 실패 시 정리(deleteQuietly)할 수 있도록 실제 파일 경로를 들고 있는다.
        private final Path file;
        private final String relativeUrl;
    }

    @Getter
    @AllArgsConstructor
    public static class ReferenceImage {
        private final byte[] data;
        private final String mimeType;
    }

    /**
     * writeToDisk가 만든 relativeUrl(/uploads/ai-images/{cover|page}/{uuid}.{ext}, 또는 그 앞에
     * 스킴/호스트가 붙은 절대 URL)을 역으로 로컬 파일 경로로 되돌려 바이트를 읽는다.
     * 캐릭터 일관성용 레퍼런스 이미지(예: 이미 생성된 표지)를 다시 읽어 Python에 넘길 때 쓴다.
     * 파일을 못 찾거나 읽기를 실패하면 예외를 던지지 않고 빈 Optional을 반환한다 - 레퍼런스 하나
     * 때문에 전체 이미지 생성 요청을 막을 이유는 없고, 호출부가 레퍼런스 없이 계속 진행하면 된다.
     */
    public Optional<ReferenceImage> readReferenceImage(String relativeUrl) {
        if (relativeUrl == null || relativeUrl.isBlank()) {
            return Optional.empty();
        }

        String imageUrlPrefix = appProperties.getUpload().getImageUrl();
        int prefixIdx = relativeUrl.indexOf(imageUrlPrefix);
        if (prefixIdx < 0) {
            log.warn("레퍼런스 이미지 URL이 예상 형식이 아닙니다: {}", relativeUrl);
            return Optional.empty();
        }

        String afterPrefix = relativeUrl.substring(prefixIdx + imageUrlPrefix.length()).replaceFirst("^/+", "");
        Path targetFile = Paths.get(appProperties.getUpload().getImageDir()).toAbsolutePath().normalize()
                .resolve(afterPrefix).normalize();

        // uploadDir 밖을 가리키는 경로(../ 등)로 조작된 URL은 거부한다.
        Path uploadDir = Paths.get(appProperties.getUpload().getImageDir()).toAbsolutePath().normalize();
        if (!targetFile.startsWith(uploadDir) || !Files.exists(targetFile)) {
            log.warn("레퍼런스 이미지 파일을 찾을 수 없습니다: url={}, file={}", relativeUrl, targetFile);
            return Optional.empty();
        }

        try {
            byte[] bytes = Files.readAllBytes(targetFile);
            String mimeType = Files.probeContentType(targetFile);
            return Optional.of(new ReferenceImage(bytes, mimeType != null ? mimeType : "image/png"));
        } catch (IOException e) {
            log.warn("레퍼런스 이미지 읽기 실패: file={}", targetFile, e);
            return Optional.empty();
        }
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
        StoredImage stored = writeToDisk(body, extension, imageType);

        log.info("Replicate 이미지 로컬 저장 완료: remoteUrl={}, file={}, size={}", remoteUrl, stored.getFile(), body.length);
        return stored;
    }

    /**
     * Gemini가 돌려주는 "data:{mimeType};base64,{data}" 형식의 이미지를 디코딩해 로컬에 저장한다.
     * imageUrl 대신 이 방식이 Python의 현재(그리고 유일한) 이미지 전달 계약이다.
     */
    public StoredImage storeFromDataUri(String dataUri, String imageType) {
        Matcher matcher = DATA_URI_PATTERN.matcher(dataUri);
        if (!matcher.matches()) {
            log.error("이미지 data URI 형식이 올바르지 않습니다: prefixLen={}", Math.min(dataUri.length(), 30));
            throw new CustomException(CommonErrorCode.IMAGE_SAVE_FAILED);
        }

        String mimeType = matcher.group(1);
        byte[] body;
        try {
            body = Base64.getDecoder().decode(matcher.group(2));
        } catch (IllegalArgumentException e) {
            log.error("이미지 base64 디코딩 실패", e);
            throw new CustomException(CommonErrorCode.IMAGE_SAVE_FAILED);
        }

        if (body.length == 0) {
            log.error("디코딩된 이미지가 0바이트입니다");
            throw new CustomException(CommonErrorCode.IMAGE_SAVE_FAILED);
        }

        String extension = resolveExtensionFromMimeType(mimeType);
        StoredImage stored = writeToDisk(body, extension, imageType);

        log.info("이미지(base64) 로컬 저장 완료: file={}, size={}", stored.getFile(), body.length);
        return stored;
    }

    private StoredImage writeToDisk(byte[] body, String extension, String imageType) {
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

    /** data URI의 mimeType(예: image/png)에서 확장자를 뽑는다. Gemini 응답의 inline_data.mime_type을 그대로 쓴다. */
    private String resolveExtensionFromMimeType(String mimeType) {
        int slashIdx = mimeType.indexOf('/');
        String subtype = (slashIdx >= 0 ? mimeType.substring(slashIdx + 1) : mimeType).toLowerCase(Locale.ROOT);
        if (subtype.equals("jpeg")) return "jpg";
        if (subtype.equals("png") || subtype.equals("webp")) return subtype;
        return DEFAULT_EXTENSION;
    }
}
