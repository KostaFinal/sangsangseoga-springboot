package com.kosta.sangsangseoga.domain.ai.controller;

import com.kosta.sangsangseoga.domain.ai.dto.AiGenerateRequestDto;
import com.kosta.sangsangseoga.domain.ai.dto.AiGenerateResponseDto;
import com.kosta.sangsangseoga.domain.ai.service.AiService;
import com.kosta.sangsangseoga.domain.ai.service.AiStreamService;
import com.kosta.sangsangseoga.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final AiStreamService aiStreamService;

    /**
     * POST /api/ai/generate
     * bookType/stage 에 따라 설정, 본문, 이미지 프롬프트 생성을 모두 처리하는 단일 엔드포인트.
     * Spring Boot가 Python FastAPI(/api/ai/generate)로 요청을 위임하고 결과를 그대로 반환한다.
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<AiGenerateResponseDto>> generate(
            @RequestBody AiGenerateRequestDto request) {
        AiGenerateResponseDto result = aiService.generate(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * POST /api/ai/generate/stream
     * Python FastAPI(/api/ai/generate/stream)의 SSE 응답을 그대로 중계한다.
     * SSE는 JSON 엔벌로프 관례(ApiResponse)의 의도적 예외이다 — 이벤트 스트림이라 감쌀 수 없다.
     */
    @PostMapping(value = "/generate/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateStream(@RequestBody AiGenerateRequestDto request) {
        return aiStreamService.streamGenerate(request);
    }
}
