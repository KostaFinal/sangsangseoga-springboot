package com.kosta.sangsangseoga.domain.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "AI 생성 응답. result는 Python(FastAPI) 응답 원본을 그대로 감싼 것이며, "
        + "{ status, taskType, message, result, missingFields, warnings, nextAction } 구조를 가진다. "
        + "status는 SUCCESS/NEED_MORE_INPUT/FAILED 중 하나다.")
public class AiGenerateResponseDto {

    @Schema(description = "요청에 실렸던 bookId를 그대로 반환")
    private Long bookId;

    @Schema(description = "요청에 실렸던 stage(taskType)를 그대로 반환")
    private String stage;

    @Schema(description = "Python(FastAPI) 응답 원본 payload")
    private Map<String, Object> result; // FastAPI 응답 원본 페이로드
}
