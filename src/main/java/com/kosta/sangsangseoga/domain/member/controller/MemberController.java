package com.kosta.sangsangseoga.domain.member.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kosta.sangsangseoga.domain.member.dto.GuardianConsentApproveRequestDto;
import com.kosta.sangsangseoga.domain.member.dto.GuardianConsentDecisionRequestDto;
import com.kosta.sangsangseoga.domain.member.dto.GuardianConsentPendingResponseDto;
import com.kosta.sangsangseoga.domain.member.dto.GuardianConsentRequestDto;
import com.kosta.sangsangseoga.domain.member.dto.GuardianConsentResponseDto;
import com.kosta.sangsangseoga.domain.member.dto.MemberMeResponseDto;
import com.kosta.sangsangseoga.domain.member.dto.ProfileImageUploadResponseDto;
import com.kosta.sangsangseoga.domain.member.dto.ViewerPreferenceDto;
import com.kosta.sangsangseoga.domain.member.dto.WithdrawRequestDto;
import com.kosta.sangsangseoga.domain.member.service.MemberService;
import com.kosta.sangsangseoga.global.common.ApiResponse;
import com.kosta.sangsangseoga.global.config.ApiErrorCodes;
import com.kosta.sangsangseoga.global.security.AuthenticationHelper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Member", description = "회원 정보/보호자 동의")
@RestController
@RequiredArgsConstructor
public class MemberController {
    // /api/members/me, /api/members/exists, /api/members/password/reset-request, /api/guardian-consents
    // /api/members/me/viewer-preference
    // 서로 다른 최상위 경로라 클래스 레벨 매핑 없이 메서드별로 전체 경로를 지정합니다.

    private final MemberService memberService;

    @Operation(summary = "내 정보 조회")
    @ApiErrorCodes({"MEMBER_NOT_FOUND"})
    @GetMapping("/api/members/me")
    public ResponseEntity<ApiResponse<MemberMeResponseDto>> getMyInfo(Authentication authentication) {
        Long memberId = AuthenticationHelper.resolveMemberId(authentication);
        return ResponseEntity.ok(ApiResponse.success(memberService.getMyInfo(memberId)));
    }

    @Operation(summary = "회원 탈퇴")
    @ApiErrorCodes({"MEMBER_NOT_FOUND", "ALREADY_DELETED_MEMBER", "WRONG_PASSWORD"})
    @DeleteMapping("/api/members/me")
    public ResponseEntity<ApiResponse<Void>> withdraw(Authentication authentication,
                                                        @RequestBody WithdrawRequestDto request) {
        Long memberId = AuthenticationHelper.resolveMemberId(authentication);
        memberService.withdraw(memberId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "프로필 사진 업로드", description = "multipart/form-data로 이미지 파일을 업로드하면 저장 후 접근 가능한 URL을 반환한다. "
            + "이 URL을 실제 프로필에 반영하려면 별도로 회원정보 수정 API를 호출해서 profileImageUrl에 저장해야 한다.")
    @ApiErrorCodes({"EMPTY_FILE", "INVALID_IMAGE_FILE"})
    @PostMapping("/api/members/me/profile-image")
    public ResponseEntity<ApiResponse<ProfileImageUploadResponseDto>> uploadProfileImage(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        AuthenticationHelper.resolveMemberId(authentication);
        return ResponseEntity.ok(ApiResponse.success(memberService.uploadProfileImage(file)));
    }

    @Operation(summary = "뷰어 설정 조회")
    @ApiErrorCodes({"MEMBER_NOT_FOUND"})
    @GetMapping("/api/members/me/viewer-preference")
    public ResponseEntity<ApiResponse<ViewerPreferenceDto>> getViewerPreference(Authentication authentication) {
        Long memberId = AuthenticationHelper.resolveMemberId(authentication);
        ViewerPreferenceDto response = memberService.getViewerPreference(memberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "뷰어 설정 변경")
    @ApiErrorCodes({"MEMBER_NOT_FOUND"})
    @PatchMapping("/api/members/me/viewer-preference")
    public ResponseEntity<ApiResponse<ViewerPreferenceDto>> updateViewerPreference(
            Authentication authentication,
            @RequestBody ViewerPreferenceDto request) {
        Long memberId = AuthenticationHelper.resolveMemberId(authentication);
        ViewerPreferenceDto response = memberService.updateViewerPreference(memberId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "보호자 동의 요청 생성", description = "보호자 이메일로 동의 요청 메일을 발송한다.")
    @ApiErrorCodes({"MEMBER_NOT_FOUND", "ALREADY_APPROVED_MEMBER"})
    @SecurityRequirements
    @PostMapping("/api/guardian-consents")
    public ResponseEntity<ApiResponse<GuardianConsentResponseDto>> requestGuardianConsent(
            @RequestBody GuardianConsentRequestDto request) {
        GuardianConsentResponseDto response = memberService.requestGuardianConsent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "내가 받은 미처리 동의 요청 목록", description = "로그인한 보호자 계정 기준으로 조회한다.")
    @ApiErrorCodes({"MEMBER_NOT_FOUND"})
    @GetMapping("/api/guardian-consents/pending")
    public ResponseEntity<ApiResponse<List<GuardianConsentPendingResponseDto>>> getPendingGuardianConsents(
            Authentication authentication) {
        Long memberId = AuthenticationHelper.resolveMemberId(authentication);
        List<GuardianConsentPendingResponseDto> response = memberService.getPendingGuardianConsents(memberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "보호자 동의 처리(이메일 링크, 비로그인)", description = "메일에 담긴 목적 한정 토큰으로 승인/거절한다.")
    @ApiErrorCodes({"GUARDIAN_CONSENT_NOT_FOUND", "GUARDIAN_CONSENT_ALREADY_PROCESSED",
            "GUARDIAN_CONSENT_EXPIRED", "INVALID_CONSENT_TOKEN", "BAD_REQUEST"})
    @SecurityRequirements
    @PatchMapping("/api/guardian-consents/{consentId}")
    public ResponseEntity<ApiResponse<GuardianConsentResponseDto>> processGuardianConsent(
            @PathVariable Long consentId,
            @RequestBody GuardianConsentApproveRequestDto request) {
        GuardianConsentResponseDto response = memberService.processGuardianConsent(consentId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "보호자 동의 처리(로그인 상태)", description = "로그인한 보호자 계정 이메일이 요청과 일치해야 한다.")
    @ApiErrorCodes({"MEMBER_NOT_FOUND", "GUARDIAN_CONSENT_NOT_FOUND", "GUARDIAN_CONSENT_ALREADY_PROCESSED",
            "GUARDIAN_CONSENT_EXPIRED", "NOT_CONSENT_GUARDIAN", "BAD_REQUEST"})
    @PatchMapping("/api/guardian-consents/{consentId}/decision")
    public ResponseEntity<ApiResponse<GuardianConsentResponseDto>> processGuardianConsentByLoggedInGuardian(
            Authentication authentication,
            @PathVariable Long consentId,
            @RequestBody GuardianConsentDecisionRequestDto request) {
        Long memberId = AuthenticationHelper.resolveMemberId(authentication);
        GuardianConsentResponseDto response =
                memberService.processGuardianConsentByLoggedInGuardian(consentId, memberId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "보호자 동의 철회", description = "ACTIVE 회원이었다면 재동의 전까지 PENDING으로 되돌린다.")
    @ApiErrorCodes({"MEMBER_NOT_FOUND", "GUARDIAN_CONSENT_NOT_FOUND", "NOT_CONSENT_GUARDIAN", "GUARDIAN_CONSENT_NOT_APPROVED"})
    @PatchMapping("/api/guardian-consents/{consentId}/withdraw")
    public ResponseEntity<ApiResponse<GuardianConsentResponseDto>> withdrawGuardianConsent(
            Authentication authentication,
            @PathVariable Long consentId) {
        Long memberId = AuthenticationHelper.resolveMemberId(authentication);
        GuardianConsentResponseDto response = memberService.withdrawGuardianConsent(consentId, memberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    
}
