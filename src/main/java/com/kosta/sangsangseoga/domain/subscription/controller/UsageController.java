package com.kosta.sangsangseoga.domain.subscription.controller;

import com.kosta.sangsangseoga.domain.subscription.dto.UsageResponseDto;
import com.kosta.sangsangseoga.domain.subscription.service.UsageService;
import com.kosta.sangsangseoga.global.common.ApiResponse;
import com.kosta.sangsangseoga.global.security.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usage/me")
@RequiredArgsConstructor
public class UsageController {

    private final UsageService usageService;

    @GetMapping
    public ResponseEntity<ApiResponse<UsageResponseDto>> getUsage(Authentication authentication) {
        Long memberId = AuthenticationHelper.resolveMemberId(authentication);
        return ResponseEntity.ok(ApiResponse.success(usageService.getUsage(memberId)));
    }
}
