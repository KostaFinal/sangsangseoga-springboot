package com.kosta.sangsangseoga.global.infra.storage;

import java.io.IOException;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * S3에 파일을 저장하고 접근 가능한 URL을 만들어준다. app.storage.type=s3일 때만 활성화된다.
 *
 * 전환 시 필요한 것:
 * - app.storage.type=s3
 * - app.s3.bucket, app.s3.region 설정(비어있으면 기동 시점에 바로 실패한다)
 * - 자격증명: 로컬은 ~/.aws/credentials 또는 환경변수, EC2는 인스턴스에 붙인 IAM Role이면
 *   DefaultCredentialsProvider가 알아서 찾는다(액세스 키를 코드/설정에 직접 넣을 필요 없음).
 * - 버킷이 private이면 app.s3.public-base-url에 CloudFront 등 실제 서빙 도메인을 지정해야
 *   반환 URL로 바로 접근 가능하다. 비워두면 S3 버킷의 리전 기본 URL을 사용한다(버킷이 public일 때만 유효).
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "app.storage", name = "type", havingValue = "s3")
public class S3FileStorageService implements FileStorageService {

    @Value("${app.s3.bucket:}")
    private String bucket;

    @Value("${app.s3.region:}")
    private String region;

    @Value("${app.s3.public-base-url:}")
    private String publicBaseUrlOverride;

    private S3Client s3Client;

    @PostConstruct
    void init() {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("app.storage.type=s3인데 app.s3.bucket이 비어 있습니다. 버킷 이름을 설정해 주세요.");
        }
        if (region == null || region.isBlank()) {
            throw new IllegalStateException("app.storage.type=s3인데 app.s3.region이 비어 있습니다. 리전을 설정해 주세요.");
        }

        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @PreDestroy
    void close() {
        if (s3Client != null) {
            s3Client.close();
        }
    }

    @Override
    public String store(MultipartFile file, String subDir) {
        String extension = extractExtension(file.getOriginalFilename());
        String key = subDir + "/" + UUID.randomUUID() + (extension != null ? "." + extension : "");

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException | SdkException e) {
            log.error("S3 업로드 중 오류 발생", e);
            throw new CustomException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }

        return resolvePublicBaseUrl() + "/" + key;
    }

    @Override
    public String store(byte[] content, String contentType, String extension, String subDir) {
        String key = subDir + "/" + UUID.randomUUID() + (extension != null && !extension.isBlank() ? "." + extension : "");

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(content));
        } catch (SdkException e) {
            log.error("S3 업로드 중 오류 발생", e);
            throw new CustomException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }

        return resolvePublicBaseUrl() + "/" + key;
    }

    @Override
    public void delete(String url) {
        String prefix = resolvePublicBaseUrl() + "/";
        if (!url.startsWith(prefix)) {
            log.warn("삭제하려는 URL이 이 버킷의 base URL과 일치하지 않습니다: {}", url);
            return;
        }
        String key = url.substring(prefix.length());

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (SdkException e) {
            log.warn("S3 객체 삭제 실패: key={}", key, e);
        }
    }

    private String resolvePublicBaseUrl() {
        return (publicBaseUrlOverride != null && !publicBaseUrlOverride.isBlank())
                ? publicBaseUrlOverride
                : String.format("https://%s.s3.%s.amazonaws.com", bucket, region);
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null) {
            return null;
        }
        int dotIdx = originalFilename.lastIndexOf('.');
        return dotIdx >= 0 ? originalFilename.substring(dotIdx + 1) : null;
    }
}
