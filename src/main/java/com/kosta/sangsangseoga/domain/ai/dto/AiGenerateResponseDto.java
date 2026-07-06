package com.kosta.sangsangseoga.domain.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiGenerateResponseDto {

    private Long bookId;
    private String stage;
    private Map<String, Object> result; // FastAPI 응답 원본 페이로드
}
