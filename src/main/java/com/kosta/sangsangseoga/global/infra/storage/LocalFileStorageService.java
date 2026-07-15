package com.kosta.sangsangseoga.global.infra.storage;

import com.kosta.sangsangseoga.global.config.AppProperties;
import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * 로컬 디스크에 파일을 저장하고 /uploads/** 로 접근 가능한 절대 URL을 만들어준다.
 * app.storage.type을 설정하지 않으면(기본값 local) 이 구현체가 활성화된다.
 * S3로 옮길 때는 app.storage.type=s3로 바꾸면 S3FileStorageService가 대신 활성화된다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.storage", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

    private final AppProperties appProperties;

    /**
     * @param file   업로드된 파일
     * @param subDir uploadDir 하위 디렉터리 이름(예: "profile-images")
     * @return 클라이언트가 바로 쓸 수 있는 절대 URL
     */
    @Override
    public String store(MultipartFile file, String subDir) {
        String extension = extractExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + (extension != null ? "." + extension : "");

        Path targetDir = Path.of(appProperties.getUploadDir(), subDir);
        Path targetPath = targetDir.resolve(fileName);

        try {
            Files.createDirectories(targetDir);
            file.transferTo(targetPath);
        } catch (IOException e) {
            log.error("파일 저장 중 오류 발생", e);
            throw new CustomException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }

        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/").path(subDir).path("/").path(fileName)
                .toUriString();
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null) {
            return null;
        }
        int dotIdx = originalFilename.lastIndexOf('.');
        return dotIdx >= 0 ? originalFilename.substring(dotIdx + 1) : null;
    }
}
