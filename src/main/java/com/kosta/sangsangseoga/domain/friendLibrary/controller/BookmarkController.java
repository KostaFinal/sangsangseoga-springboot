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
     * 북마크 등록/이동 - 201
     * Request Body: { "pageNo": 5 }
     * 책당 북마크가 하나뿐이라 이미 있으면 이 페이지로 옮겨진다.
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
     * DELETE /api/books/:id/bookmarks
     * 북마크 취소 - 204 (책당 북마크가 하나뿐이라 페이지 구분 불필요)
     */
    @DeleteMapping("/{id}/bookmarks")
    public ResponseEntity<Void> removeBookmark(
            @PathVariable Long id,
            @AuthenticationPrincipal Long memberId) throws Exception {
        bookmarkService.removeBookmark(memberId, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/books/:id/bookmarks
     * 내가 북마크한 페이지 조회 (없으면 isBookmarkedByMe=false, pageNo=null)
     */
    @GetMapping("/{id}/bookmarks")
    public ResponseEntity<ApiResponse<BookmarkDto>> getBookmark(
            @PathVariable Long id,
            @AuthenticationPrincipal Long memberId) throws Exception {
        BookmarkDto result = bookmarkService.getBookmark(memberId, id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}