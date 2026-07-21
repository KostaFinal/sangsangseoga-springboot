package com.kosta.sangsangseoga.global.config;

import com.kosta.sangsangseoga.global.exception.ErrorCode;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    private static final String JWT_SCHEME_NAME = "bearerAuth";

    private final ErrorCodeRegistry errorCodeRegistry;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("상상서가 API")
                        .description("상상서가 서비스 API 문서")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(JWT_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(JWT_SCHEME_NAME, new SecurityScheme()
                                .name(JWT_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    @Bean
    public OperationCustomizer errorCodeCustomizer() {
        return (operation, handlerMethod) -> {
            ApiErrorCodes codes = handlerMethod.getMethodAnnotation(ApiErrorCodes.class);
            if (codes == null || codes.value().length == 0)
                return operation;

            // 1. 상태코드별로 그룹핑 (같은 400끼리, 같은 404끼리 묶기 위함 - 키 중복 덮어쓰기 방지)
            Map<Integer, List<ErrorCode>> errorGroups = Arrays.stream(codes.value())
                .map(errorCodeRegistry::resolve)
                .collect(Collectors.groupingBy(code -> code.getStatus().value()));

            // 2. 상태코드 하나당 Response 하나. 그 안에 에러코드 개수만큼 example을 추가
            errorGroups.forEach((statusCode, errorCodes) -> {
                MediaType mediaType = new MediaType();

                errorCodes.forEach(code -> {
                    String jsonExample = String.format(
                        "{\"success\":false, \"data\":null, \"code\":\"%s\", \"message\":\"%s\"}",
                        code.name(), code.getMessage()
                    );

                    mediaType.addExamples(code.name(),
                        new Example().summary(code.getMessage()).value(jsonExample));
                });

                Content content = new Content().addMediaType("application/json", mediaType);

                ApiResponse apiResponse = operation.getResponses().computeIfAbsent(String.valueOf(statusCode),
                    key -> new ApiResponse().description("비즈니스 에러 발생"));

                apiResponse.setContent(content);
            });
            return operation;
        };
    }
}
