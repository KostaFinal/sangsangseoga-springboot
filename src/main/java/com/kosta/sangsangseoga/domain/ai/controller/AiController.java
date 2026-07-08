package com.kosta.sangsangseoga.domain.ai.controller;

import com.kosta.sangsangseoga.domain.ai.dto.AiGenerateImageRequestDto;
import com.kosta.sangsangseoga.domain.ai.dto.AiGenerateImageResponseDto;
import com.kosta.sangsangseoga.domain.ai.dto.AiGenerateRequestDto;
import com.kosta.sangsangseoga.domain.ai.dto.AiGenerateResponseDto;
import com.kosta.sangsangseoga.domain.ai.service.AiImageService;
import com.kosta.sangsangseoga.domain.ai.service.AiService;
import com.kosta.sangsangseoga.domain.ai.service.AiStreamService;
import com.kosta.sangsangseoga.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI 생성", description = "동화/소설 등 도서 창작 과정에서 쓰이는 단일 AI 생성 엔드포인트(설정 수집/선택지/페이지 계획/본문 작성/본문 수정 등)")
public class AiController {

    private final AiService aiService;
    private final AiStreamService aiStreamService;
    private final AiImageService aiImageService;

    /**
     * POST /api/ai/generate
     * bookType/stage 에 따라 설정, 본문, 이미지 프롬프트 생성을 모두 처리하는 단일 엔드포인트.
     * Spring Boot가 Python FastAPI(/api/ai/generate)로 요청을 위임하고 결과를 그대로 반환한다.
     */
    @Operation(
            summary = "AI 콘텐츠 생성",
            description = "stage(taskType)에 따라 설정 수집, 선택지 생성, 페이지 계획, 본문 작성/수정, 장면 계획, 삽화 프롬프트 생성을 "
                    + "모두 처리하는 단일 엔드포인트다. Spring은 요청을 Python FastAPI(/api/ai/generate)로 위임하고 응답을 그대로 감싸서 반환한다."
    )
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
    @Operation(
            summary = "AI 콘텐츠 생성(스트리밍)",
            description = "/generate와 동일한 요청 바디를 받아 Python의 SSE 스트림(event: delta/done/error)을 그대로 중계한다. "
                    + "스트리밍 텍스트는 로딩 중 미리보기 용도일 뿐이며, 최종 결과는 항상 /generate(non-stream) 호출로 확정한다."
    )
    @PostMapping(value = "/generate/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateStream(@RequestBody AiGenerateRequestDto request) {
        return aiStreamService.streamGenerate(request);
    }

    /**
     * POST /api/ai/generate-image
     * promptText/imageType/pageNo/style/aspectRatio를 받아 Python(FastAPI) /api/ai/generate-image로
     * 위임하고, Replicate가 생성한 imageUrl을 그대로 반환한다.
     * book_image 저장, S3 업로드 등은 이번 범위에 포함하지 않는다(AiImageService의 TODO 참고).
     */
    @Operation(
            summary = "AI 이미지 생성",
            description = "promptText/imageType/pageNo/style/aspectRatio를 받아 Python(FastAPI)의 Replicate 호출을 위임하고 "
                    + "imageUrl을 그대로 반환한다. book_image 저장/S3 업로드는 이번 범위에 포함하지 않는다."
    )
    @PostMapping("/generate-image")
    public ResponseEntity<ApiResponse<AiGenerateImageResponseDto>> generateImage(
           @Valid @RequestBody AiGenerateImageRequestDto request) {
        AiGenerateImageResponseDto result = aiImageService.generateImage(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
