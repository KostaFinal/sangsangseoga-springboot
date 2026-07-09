package com.kosta.sangsangseoga.domain.book.controller;

import com.kosta.sangsangseoga.domain.book.dto.BookContentsResponseDto;
import com.kosta.sangsangseoga.domain.book.dto.BookDetailDto;
import com.kosta.sangsangseoga.domain.book.dto.BookListResponseDto;
import com.kosta.sangsangseoga.domain.book.dto.BookPublishRequestDto;
import com.kosta.sangsangseoga.domain.book.dto.BookPublishResponseDto;
import com.kosta.sangsangseoga.domain.book.dto.BookRecommendResponseDto;
import com.kosta.sangsangseoga.domain.book.service.BookService;
import com.kosta.sangsangseoga.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookListController {

    private final BookService bookService;

    /**
     * POST /api/books
     * 책 생성(최종 저장) - 201
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BookPublishResponseDto>> publish(
            @RequestBody BookPublishRequestDto request,
            @AuthenticationPrincipal Long memberId) {
        BookPublishResponseDto result = bookService.publish(memberId, request);
        return ResponseEntity.status(201).body(ApiResponse.success(result));
    }

    /**
     * GET /api/books
     * 책 목록 조회 - 200
     */
    @GetMapping
    public ResponseEntity<ApiResponse<BookListResponseDto>> getBooks(
            @RequestParam(required = false) String bookType,
            @RequestParam(required = false, defaultValue = "latest") String sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "12") int size,
            @AuthenticationPrincipal Long memberId) throws Exception {
        BookListResponseDto result = bookService.getBooks(bookType, sort, keyword, page, size, memberId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * GET /api/books/my
     * 내가 쓴 책 목록 조회 - 200
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<BookListResponseDto>> getMyBooks(
            @AuthenticationPrincipal Long memberId) throws Exception {

        BookListResponseDto result = bookService.getMyBooks(memberId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * GET /api/books/:id
     * 책 상세 조회 - 200
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookDetailDto>> getBook(
            @PathVariable Long id,
            @AuthenticationPrincipal Long memberId) throws Exception {
        BookDetailDto result = bookService.getBook(id, memberId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * GET /api/books/:id/contents
     * 책 본문(페이지) 조회 - 200
     */
    @GetMapping("/{id}/contents")
    public ResponseEntity<ApiResponse<BookContentsResponseDto>> getContents(
            @PathVariable Long id) throws Exception {
        BookContentsResponseDto result = bookService.getContents(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * GET /api/books/:id/recommendations
     * 함께 읽기 좋은 작품 추천 - 200
     */
    @GetMapping("/{id}/recommendations")
    public ResponseEntity<ApiResponse<BookRecommendResponseDto>> getRecommendations(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "4") int size) throws Exception {
        BookRecommendResponseDto result = bookService.getRecommendations(id, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}