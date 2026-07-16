package com.kosta.sangsangseoga.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * AiImageStorageService가 app.upload.image-dir 아래 저장한 AI 생성 이미지를 app.upload.image-url
 * 경로로 정적 파일처럼 서빙한다. src/main/resources/static이 아니라 프로젝트 외부 디렉터리를 매핑하는
 * 이유는 AppProperties.Upload와 동일 - jar에 포함되는 classpath 경로는 재배포마다 초기화된다.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AppProperties appProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get(appProperties.getUpload().getImageDir()).toAbsolutePath().normalize();
        String location = uploadDir.toUri().toString();
        if (!location.endsWith("/")) {
            location += "/";
        }

        registry.addResourceHandler(appProperties.getUpload().getImageUrl() + "/**")
                .addResourceLocations(location);
    }
}
