package com.kosta.sangsangseoga.domain.myLibrary.controller;

import com.kosta.sangsangseoga.domain.myLibrary.dto.ReadingMemoDto;
import com.kosta.sangsangseoga.domain.myLibrary.service.ReadingMemoService;
import com.kosta.sangsangseoga.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/books/{bookId}/pages/{pageNo}/memos")
@RequiredArgsConstructor
public class ReadingMemoController {

    private final ReadingMemoService readingMemoService;

    /**
     * GET /api/books/:bookId/pages/:pageNo/memos
     * 메모 조회 - 200 (메모 없으면 data: null)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<ReadingMemoDto>> getMemo(
            @PathVariable Long bookId,
            @PathVariable Integer pageNo,
            @AuthenticationPrincipal Long memberId) throws Exception {
        ReadingMemoDto result = readingMemoService.getMemo(memberId, bookId, pageNo);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * POST /api/books/:bookId/pages/:pageNo/memos
     * 메모 작성 - 201
     * Request Body: { "content": "...", "posX": 0.3, "posY": 0.5 }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReadingMemoDto>> addMemo(
            @PathVariable Long bookId,
            @PathVariable Integer pageNo,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal Long memberId) throws Exception {
        String content = (String) body.get("content");
        BigDecimal posX = body.get("posX") != null ? new BigDecimal(body.get("posX").toString()) : null;
        BigDecimal posY = body.get("posY") != null ? new BigDecimal(body.get("posY").toString()) : null;
        ReadingMemoDto result = readingMemoService.addMemo(memberId, bookId, pageNo, content, posX, posY);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result));
    }

    /**
     * PATCH /api/books/:bookId/pages/:pageNo/memos
     * 메모 수정 - 200
     * Request Body: { "content": "...", "posX": 0.4, "posY": 0.6 }
     */
    @PatchMapping
    public ResponseEntity<ApiResponse<ReadingMemoDto>> updateMemo(
            @PathVariable Long bookId,
            @PathVariable Integer pageNo,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal Long memberId) throws Exception {
        String content = body.get("content") != null ? (String) body.get("content") : null;
        BigDecimal posX = body.get("posX") != null ? new BigDecimal(body.get("posX").toString()) : null;
        BigDecimal posY = body.get("posY") != null ? new BigDecimal(body.get("posY").toString()) : null;
        ReadingMemoDto result = readingMemoService.updateMemo(memberId, bookId, pageNo, content, posX, posY);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * DELETE /api/books/:bookId/pages/:pageNo/memos
     * 메모 삭제 - 204
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteMemo(
            @PathVariable Long bookId,
            @PathVariable Integer pageNo,
            @AuthenticationPrincipal Long memberId) throws Exception {
        readingMemoService.deleteMemo(memberId, bookId, pageNo);
        return ResponseEntity.noContent().build();
    }
}