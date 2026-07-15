package com.kosta.sangsangseoga.global.infra.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 업로드 파일 저장 계약. 구현체는 app.storage.type 설정값(local/s3)으로 선택된다.
 * 지금은 LocalFileStorageService만 활성화되어 있고, S3FileStorageService는
 * 나중에 S3로 옮길 때 app.storage.type=s3와 자격증명만 채우면 바로 쓸 수 있게 미리 만들어둔 것이다.
 */
public interface FileStorageService {

    /**
     * @param file   업로드된 파일
     * @param subDir 저장소 하위 디렉터리(또는 S3 key prefix) 이름(예: "profile-images")
     * @return 클라이언트가 바로 쓸 수 있는 절대 URL
     */
    String store(MultipartFile file, String subDir);
}
