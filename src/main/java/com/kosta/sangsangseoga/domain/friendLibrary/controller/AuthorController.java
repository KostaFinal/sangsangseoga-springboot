package com.kosta.sangsangseoga.domain.friendLibrary.controller;

import com.kosta.sangsangseoga.domain.friendLibrary.dto.AuthorListResponseDto;
import com.kosta.sangsangseoga.domain.friendLibrary.service.AuthorService;
import com.kosta.sangsangseoga.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    /**
     * GET /api/authors
     * 작가 검색 - 200
     * 인증 불필요 (비로그인도 조회 가능한 공개 API)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<AuthorListResponseDto>> getAuthors(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "followers") String sort,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "20") int size) throws Exception {
        AuthorListResponseDto result = authorService.getAuthors(keyword, sort, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
