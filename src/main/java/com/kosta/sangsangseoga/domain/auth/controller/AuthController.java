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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponseDto>> signup(@RequestBody SignupRequestDto request) {
        SignupResponseDto response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@RequestBody LoginRequestDto request) {
        LoginResponseDto response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        authService.logout(memberId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/token-refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponseDto>> refreshToken(@RequestBody TokenRefreshRequestDto request) {
        TokenRefreshResponseDto response = authService.refreshAccessToken(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/password/reset_request")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@RequestBody PasswordResetRequestDto request) {
        authService.requestPasswordReset(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    @PatchMapping("/password/reset")
    public ResponseEntity<ApiResponse<Void>> completePasswordReset(@RequestBody PasswordResetCompleteDto request) {
        authService.completePasswordReset(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
