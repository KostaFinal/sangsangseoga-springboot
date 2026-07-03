package com.kosta.sangsangseoga.domain.friendLibrary.controller;

import com.kosta.sangsangseoga.domain.friendLibrary.dto.AuthorFollowDto;
import com.kosta.sangsangseoga.domain.friendLibrary.service.AuthorFollowService;
import com.kosta.sangsangseoga.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorFollowController {

    private final AuthorFollowService authorFollowService;

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