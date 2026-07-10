package com.kosta.sangsangseoga.domain.subscription.controller;

import com.kosta.sangsangseoga.domain.subscription.dto.UsageResponseDto;
import com.kosta.sangsangseoga.domain.subscription.service.UsageService;
import com.kosta.sangsangseoga.global.common.ApiResponse;
import com.kosta.sangsangseoga.global.config.ApiErrorCodes;
import com.kosta.sangsangseoga.global.security.AuthenticationHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Usage", description = "AI 생성 사용량")
@RestController
@RequestMapping("/api/usage/me")
@RequiredArgsConstructor
public class UsageController {

    private final UsageService usageService;

    @Operation(summary = "오늘 사용량 조회", description = "PREMIUM은 일일 잔여량, FREE는 생애 체험 잔여 호출수를 반환한다.")
    @ApiErrorCodes({"MEMBER_NOT_FOUND"})
    @GetMapping
    public ResponseEntity<ApiResponse<UsageResponseDto>> getUsage(Authentication authentication) {
        Long memberId = AuthenticationHelper.resolveMemberId(authentication);
        return ResponseEntity.ok(ApiResponse.success(usageService.getUsage(memberId)));
    }
}
