package com.kosta.sangsangseoga.domain.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "AI 이미지 생성 응답. Python(FastAPI) 응답을 그대로 감싼 것이다.")
public class AiGenerateImageResponseDto {

    @Schema(description = "이미지 생성 성공 여부")
    private boolean success;

    @Schema(description = "결과 메시지")
    private String message;

    @Schema(description = "생성된 이미지의 호스팅 URL. Gemini 이미지 생성은 URL을 주지 않아 항상 null이고 imageBase64를 대신 쓴다.")
    private String imageUrl;

    @Schema(description = "이미지 data URI(예: data:image/png;base64,...). <img src>에 바로 쓸 수 있는 형태로 온다.")
    private String imageBase64;
}
