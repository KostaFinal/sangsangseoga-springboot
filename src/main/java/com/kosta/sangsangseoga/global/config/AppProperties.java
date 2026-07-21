package com.kosta.sangsangseoga.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String frontendUrl;
    private String mailFrom;
    // 로컬 디스크에 저장하는 업로드 파일(프로필 사진 등)의 루트 경로 (LocalFileStorageService 참고).
    private String uploadDir;
    private final Upload upload = new Upload();

    @Getter
    @Setter
    public static class Upload {
        // Replicate 이미지 다운로드본을 저장하는 로컬 디렉터리(프로젝트 외부 경로 권장 - jar 빌드에 포함되지
        // 않아야 재배포 후에도 남아있는다). 상대 경로면 서버 프로세스의 작업 디렉터리 기준으로 해석된다.
        private String imageDir;
        // 위 디렉터리를 정적 리소스로 서빙하는 URL prefix (WebConfig의 addResourceHandlers 참고).
        private String imageUrl;
    }
}