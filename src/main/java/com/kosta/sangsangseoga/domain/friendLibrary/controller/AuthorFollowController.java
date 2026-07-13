package com.kosta.sangsangseoga.domain.friendLibrary.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kosta.sangsangseoga.domain.friendLibrary.dto.AuthorFollowDto;
import com.kosta.sangsangseoga.domain.friendLibrary.dto.AuthorListResponseDto;
import com.kosta.sangsangseoga.domain.friendLibrary.service.AuthorFollowService;
import com.kosta.sangsangseoga.global.common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorFollowController {

    private final AuthorFollowService authorFollowService;
    
    /**
     * GET /api/authors/follows/me
     * 내가 팔로우한 관심 작가 목록 조회 - 200
     */

    @GetMapping("/follows/me")
    public ResponseEntity<ApiResponse<AuthorListResponseDto>> getMyFollowedAuthors(
            @AuthenticationPrincipal Long memberId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size)throws Exception{

        AuthorListResponseDto result = authorFollowService.getMyFollowedAuthors(memberId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * POST /api/authors/:id/follows
     * 작가 팔로우 - 201
     */
    @PostMapping("/{id}/follows")
    public ResponseEntity<ApiResponse<AuthorFollowDto>> follow(
            @PathVariable Long id,
            @AuthenticationPrincipal Long memberId) throws Exception {
        AuthorFollowDto result = authorFollowService.follow(memberId, id);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result));
    }

    /**
     * DELETE /api/authors/:id/follows
     * 작가 언팔로우 - 204
     */
    @DeleteMapping("/{id}/follows")
    public ResponseEntity<Void> unfollow(
            @PathVariable Long id,
            @AuthenticationPrincipal Long memberId) throws Exception {
        authorFollowService.unfollow(memberId, id);
        return ResponseEntity.noContent().build();
    }
}