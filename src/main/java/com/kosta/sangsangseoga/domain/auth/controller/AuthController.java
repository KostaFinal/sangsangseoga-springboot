package com.kosta.sangsangseoga.domain.auth.controller;

import com.kosta.sangsangseoga.domain.auth.dto.LoginRequestDto;
import com.kosta.sangsangseoga.domain.auth.dto.LoginResponseDto;
import com.kosta.sangsangseoga.domain.auth.dto.PasswordResetCompleteDto;
import com.kosta.sangsangseoga.domain.auth.dto.PasswordResetRequestDto;
import com.kosta.sangsangseoga.domain.auth.dto.SignupRequestDto;
import com.kosta.sangsangseoga.domain.auth.dto.SignupResponseDto;
import com.kosta.sangsangseoga.domain.auth.dto.TokenRefreshRequestDto;
import com.kosta.sangsangseoga.domain.auth.dto.TokenRefreshResponseDto;
import com.kosta.sangsangseoga.domain.auth.service.AuthService;
import com.kosta.sangsangseoga.global.common.ApiResponse;
import com.kosta.sangsangseoga.global.config.ApiErrorCodes;
import com.kosta.sangsangseoga.global.security.AuthenticationHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "Auth", description = "회원가입/로그인/토큰")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "만 14세 미만은 보호자 동의 대기(PENDING)로 가입되며 토큰이 발급되지 않는다"
            + "(응답에 pendingGuardianConsent=true만 내려감). 그 외에는 즉시 ACTIVE로 가입되어 토큰이 함께 발급된다.")
    @ApiErrorCodes({"DUPLICATE_EMAIL", "DUPLICATE_NICKNAME", "INVALID_BIRTH_DATE"})
    @SecurityRequirements
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponseDto>> signup(@Valid @RequestBody SignupRequestDto request) {
        SignupResponseDto response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "로그인")
    @ApiErrorCodes({"LOGIN_FAILED", "SUSPENDED_MEMBER", "DELETED_MEMBER", "PENDING_GUARDIAN_CONSENT"})
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@RequestBody LoginRequestDto request) {
        LoginResponseDto response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "로그아웃", description = "Redis에 저장된 Refresh Token을 즉시 삭제한다.")
    @ApiErrorCodes({})
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication) {
        Long memberId = AuthenticationHelper.resolveMemberId(authentication);
        authService.logout(memberId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "Access Token 재발급", description = "Refresh Token은 갱신하지 않는다.")
    @ApiErrorCodes({"EXPIRED_REFRESH_TOKEN", "INVALID_REFRESH_TOKEN", "MEMBER_NOT_FOUND"})
    @SecurityRequirements
    @PostMapping("/token-refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponseDto>> refreshToken(@RequestBody TokenRefreshRequestDto request) {
        TokenRefreshResponseDto response = authService.refreshAccessToken(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "비밀번호 재설정 메일 발송 요청", description = "소셜 로그인으로 가입한 계정은 이용할 수 없다.")
    @ApiErrorCodes({"MEMBER_NOT_FOUND", "OAUTH_ACCOUNT_PASSWORD_RESET_NOT_ALLOWED"})
    @SecurityRequirements
    @PostMapping("/password/reset_request")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@RequestBody PasswordResetRequestDto request) {
        authService.requestPasswordReset(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    @Operation(summary = "비밀번호 재설정 완료")
    @ApiErrorCodes({"WEAK_PASSWORD", "EXPIRED_RESET_TOKEN", "INVALID_RESET_TOKEN", "MEMBER_NOT_FOUND"})
    @SecurityRequirements
    @PatchMapping("/password/reset")
    public ResponseEntity<ApiResponse<Void>> completePasswordReset(@RequestBody PasswordResetCompleteDto request) {
        authService.completePasswordReset(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
