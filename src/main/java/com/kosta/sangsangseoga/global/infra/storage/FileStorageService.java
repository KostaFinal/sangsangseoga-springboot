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

    /**
     * MultipartFile이 아니라 바이트 배열로 이미 들고 있는 파일(예: AI가 생성한 이미지, base64 디코딩
     * 결과)을 저장할 때 쓴다. 원본 파일명이 없으므로 확장자를 직접 넘긴다.
     *
     * @param content     저장할 바이트
     * @param contentType MIME 타입(예: "image/png")
     * @param extension   확장자(점 없이, 예: "png"). null이면 확장자 없이 저장한다.
     * @param subDir      저장소 하위 디렉터리(또는 S3 key prefix) 이름
     * @return 클라이언트가 바로 쓸 수 있는 절대 URL
     */
    String store(byte[] content, String contentType, String extension, String subDir);

    /**
     * store()가 반환했던 URL을 기준으로 실제 저장소에서 파일/객체를 삭제한다.
     * 저장 이후 단계(사용량 기록 등)가 실패해 보상 삭제가 필요할 때 쓴다.
     */
    void delete(String url);
}
