package com.kosta.sangsangseoga.domain.friendLibrary.controller;

import com.kosta.sangsangseoga.domain.friendLibrary.dto.CommentDto;
import com.kosta.sangsangseoga.domain.friendLibrary.dto.CommentUpdateDto;
import com.kosta.sangsangseoga.domain.friendLibrary.service.CommentService;
import com.kosta.sangsangseoga.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * POST /api/books/:id/comments
     * 댓글 작성 - 201
     * Request Body: { "content": "...", "replyToCommentId": 21 (optional) }
     */
    @PostMapping("/api/books/{id}/comments")
    public ResponseEntity<ApiResponse<CommentDto>> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal Long memberId) throws Exception {
        String content = (String) body.get("content");
        Long replyToCommentId = body.get("replyToCommentId") != null
                ? Long.valueOf(body.get("replyToCommentId").toString()) : null;
        CommentDto result = commentService.addComment(memberId, id, content, replyToCommentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result));
    }

    /**
     * POST /api/comments/:id/replies
     * 답글 작성 - 201
     * Request Body: { "content": "..." }
     */
    @PostMapping("/api/comments/{id}/replies")
    public ResponseEntity<ApiResponse<CommentDto>> addReply(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal Long memberId) throws Exception {
        String content = body.get("content");
        CommentDto result = commentService.addReply(memberId, id, content);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result));
    }

    /**
     * PATCH /api/comments/:id
     * 댓글 수정 - 200
     * Request Body: { "content": "..." }
     */
    @PatchMapping("/api/comments/{id}")
    public ResponseEntity<ApiResponse<CommentUpdateDto>> updateComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal Long memberId) throws Exception {
        String content = body.get("content");
        commentService.updateComment(memberId, id, content);

        // 컨트롤러에서 응답 직접 조립
        CommentUpdateDto result = CommentUpdateDto.builder()
                .id(id)
                .content(content)
                .updatedAt(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * DELETE /api/comments/:id
     * 댓글 삭제 (소프트 딜리트) - 204
     */
    @DeleteMapping("/api/comments/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            @AuthenticationPrincipal Long memberId) throws Exception {
        commentService.deleteComment(memberId, id);
        return ResponseEntity.noContent().build();
    }
}