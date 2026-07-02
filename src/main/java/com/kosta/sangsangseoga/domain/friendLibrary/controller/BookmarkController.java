package com.kosta.sangsangseoga.domain.friendLibrary.controller;

import com.kosta.sangsangseoga.domain.friendLibrary.dto.BookmarkDto;
import com.kosta.sangsangseoga.domain.friendLibrary.service.BookmarkService;
import com.kosta.sangsangseoga.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    /**
     * POST /api/books/:id/bookmarks
     * 북마크 등록 - 201
     * Request Body: { "pageNo": 5 }
     */
    @PostMapping("/{id}/bookmarks")
    public ResponseEntity<ApiResponse<BookmarkDto>> addBookmark(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body,
            @AuthenticationPrincipal Long memberId) throws Exception {
        Integer pageNo = body.get("pageNo");
        BookmarkDto result = bookmarkService.addBookmark(memberId, id, pageNo);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result));
    }

    /**
     * DELETE /api/books/:id/bookmarks?pageNo=5
     * 북마크 취소 - 204
     */
    @DeleteMapping("/{id}/bookmarks")
    public ResponseEntity<Void> removeBookmark(
            @PathVariable Long id,
            @RequestParam Integer pageNo,
            @AuthenticationPrincipal Long memberId) throws Exception {
        bookmarkService.removeBookmark(memberId, id, pageNo);
        return ResponseEntity.noContent().build();
    }
}