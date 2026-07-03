package com.kosta.sangsangseoga.domain.friendLibrary.controller;

import com.kosta.sangsangseoga.domain.friendLibrary.dto.BookLikeDto;
import com.kosta.sangsangseoga.domain.friendLibrary.service.BookLikeService;
import com.kosta.sangsangseoga.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookLikeController {

    private final BookLikeService bookLikeService;

    /**
     * POST /api/books/:id/likes
     * 좋아요 추가 - 201
     */
    @PostMapping("/{id}/likes")
    public ResponseEntity<ApiResponse<BookLikeDto>> like(
            @PathVariable Long id,
            @AuthenticationPrincipal Long memberId) throws Exception {
        BookLikeDto result = bookLikeService.like(memberId, id);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result));
    }

    /**
     * DELETE /api/books/:id/likes
     * 좋아요 취소 - 204
     */
    @DeleteMapping("/{id}/likes")
    public ResponseEntity<Void> unlike(
            @PathVariable Long id,
            @AuthenticationPrincipal Long memberId) throws Exception {
        bookLikeService.unlike(memberId, id);
        return ResponseEntity.noContent().build();
    }
}