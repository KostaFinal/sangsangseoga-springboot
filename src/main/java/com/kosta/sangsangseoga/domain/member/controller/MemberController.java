package com.kosta.sangsangseoga.domain.member.controller;

import com.kosta.sangsangseoga.domain.member.dto.GuardianConsentApproveRequestDto;
import com.kosta.sangsangseoga.domain.member.dto.GuardianConsentDecisionRequestDto;
import com.kosta.sangsangseoga.domain.member.dto.GuardianConsentPendingResponseDto;
import com.kosta.sangsangseoga.domain.member.dto.GuardianConsentRequestDto;
import com.kosta.sangsangseoga.domain.member.dto.GuardianConsentResponseDto;
import com.kosta.sangsangseoga.domain.member.dto.WithdrawRequestDto;
import com.kosta.sangsangseoga.domain.member.service.MemberService;
import com.kosta.sangsangseoga.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {
    // /api/members/me, /api/members/exists, /api/members/password/reset-request, /api/guardian-consents
    // 서로 다른 최상위 경로라 클래스 레벨 매핑 없이 메서드별로 전체 경로를 지정합니다.

    private final MemberService memberService;

    @DeleteMapping("/api/members/me")
    public ResponseEntity<ApiResponse<Void>> withdraw(Authentication authentication,
                                                        @RequestBody WithdrawRequestDto request) {
        Long memberId = (Long) authentication.getPrincipal();
        memberService.withdraw(memberId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/api/guardian-consents")
    public ResponseEntity<ApiResponse<GuardianConsentResponseDto>> requestGuardianConsent(
            @RequestBody GuardianConsentRequestDto request) {
        GuardianConsentResponseDto response = memberService.requestGuardianConsent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/api/guardian-consents/pending")
    public ResponseEntity<ApiResponse<List<GuardianConsentPendingResponseDto>>> getPendingGuardianConsents(
            Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        List<GuardianConsentPendingResponseDto> response = memberService.getPendingGuardianConsents(memberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/api/guardian-consents/{consentId}")
    public ResponseEntity<ApiResponse<GuardianConsentResponseDto>> processGuardianConsent(
            @PathVariable Long consentId,
            @RequestBody GuardianConsentApproveRequestDto request) {
        GuardianConsentResponseDto response = memberService.processGuardianConsent(consentId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/api/guardian-consents/{consentId}/decision")
    public ResponseEntity<ApiResponse<GuardianConsentResponseDto>> processGuardianConsentByLoggedInGuardian(
            Authentication authentication,
            @PathVariable Long consentId,
            @RequestBody GuardianConsentDecisionRequestDto request) {
        Long memberId = (Long) authentication.getPrincipal();
        GuardianConsentResponseDto response =
                memberService.processGuardianConsentByLoggedInGuardian(consentId, memberId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/api/guardian-consents/{consentId}/withdraw")
    public ResponseEntity<ApiResponse<GuardianConsentResponseDto>> withdrawGuardianConsent(
            Authentication authentication,
            @PathVariable Long consentId) {
        Long memberId = (Long) authentication.getPrincipal();
        GuardianConsentResponseDto response = memberService.withdrawGuardianConsent(consentId, memberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
