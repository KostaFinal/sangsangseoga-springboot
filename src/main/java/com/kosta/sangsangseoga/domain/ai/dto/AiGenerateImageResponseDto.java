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

    @Schema(description = "생성된 이미지의 영구 URL. Replicate의 임시 delivery URL을 Spring이 즉시 다운로드해 " +
            "로컬(app.upload.image-dir)에 저장한 뒤 서빙하는 절대 URL이다 - Replicate URL은 여기 담기지 않는다.")
    private String imageUrl;

    @Schema(description = "이미지 base64 데이터. 현재 구현에서는 항상 null이며 imageUrl만 사용한다.")
    private String imageBase64;
}
