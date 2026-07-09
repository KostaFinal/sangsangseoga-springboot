package com.kosta.sangsangseoga.domain.auth.controller;

import com.kosta.sangsangseoga.domain.auth.dto.OAuthAuthorizeUrlResponseDto;
import com.kosta.sangsangseoga.domain.auth.dto.OAuthCallbackRequestDto;
import com.kosta.sangsangseoga.domain.auth.dto.OAuthCallbackResponseDto;
import com.kosta.sangsangseoga.domain.auth.dto.OAuthCompleteSignupRequestDto;
import com.kosta.sangsangseoga.domain.auth.service.OAuthService;
import com.kosta.sangsangseoga.global.common.ApiResponse;
import com.kosta.sangsangseoga.global.config.ApiErrorCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "OAuth", description = "카카오/네이버 소셜 로그인")
@RestController
@RequestMapping("/api/auth/oauth/{provider}")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oAuthService;

    @Operation(summary = "소셜 로그인 인가 URL 발급", description = "provider(kakao|naver)의 client_id를 조합한 완성된 인가 URL을 내려준다.")
    @ApiErrorCodes({"UNSUPPORTED_OAUTH_PROVIDER"})
    @GetMapping("/authorize-url")
    public ResponseEntity<ApiResponse<OAuthAuthorizeUrlResponseDto>> getAuthorizeUrl(
            @PathVariable String provider,
            @RequestParam String redirectUri) {
        return ResponseEntity.ok(ApiResponse.success(oAuthService.getAuthorizeUrl(provider, redirectUri)));
    }

    @Operation(summary = "소셜 로그인/가입 콜백", description = "기존 회원이면 로그인 처리, 신규 회원이면 제공자가 생년월일을 줬는지에 따라 "
            + "즉시 가입(성인/미성년 분기) 또는 oauthSignupToken 발급으로 갈린다.")
    @ApiErrorCodes({"UNSUPPORTED_OAUTH_PROVIDER", "OAUTH_AUTH_FAILED", "OAUTH_EMAIL_REQUIRED",
            "SUSPENDED_MEMBER", "DELETED_MEMBER", "PENDING_GUARDIAN_CONSENT",
            "DUPLICATE_EMAIL", "DUPLICATE_NICKNAME", "INVALID_BIRTH_DATE"})
    @PostMapping("/callback")
    public ResponseEntity<ApiResponse<OAuthCallbackResponseDto>> callback(
            @PathVariable String provider,
            @Valid @RequestBody OAuthCallbackRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(oAuthService.handleCallback(provider, request)));
    }

    @Operation(summary = "소셜 신규가입 완료", description = "콜백에서 생년월일을 못 받았을 때만 호출한다. "
            + "oauthSignupToken + 사용자가 입력한 닉네임/생년월일로 가입을 마무리한다.")
    @ApiErrorCodes({"INVALID_OAUTH_SIGNUP_TOKEN", "EXPIRED_OAUTH_SIGNUP_TOKEN",
            "DUPLICATE_EMAIL", "DUPLICATE_NICKNAME", "INVALID_BIRTH_DATE"})
    @PostMapping("/complete-signup")
    public ResponseEntity<ApiResponse<OAuthCallbackResponseDto>> completeSignup(
            @PathVariable String provider,
            @Valid @RequestBody OAuthCompleteSignupRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(oAuthService.completeSignup(request)));
    }
}
