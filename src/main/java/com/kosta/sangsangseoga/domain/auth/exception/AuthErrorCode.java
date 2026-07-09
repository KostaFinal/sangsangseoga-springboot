package com.kosta.sangsangseoga.domain.auth.exception;

import com.kosta.sangsangseoga.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode implements ErrorCode {

    // ===== Login =====
    LOGIN_FAILED(HttpStatus.BAD_REQUEST, "이메일 또는 비밀번호가 일치하지 않습니다."),
    SUSPENDED_MEMBER(HttpStatus.FORBIDDEN, "이용이 정지된 계정입니다. 고객센터에 문의해 주세요."),
    DELETED_MEMBER(HttpStatus.NOT_FOUND, "존재하지 않거나 탈퇴한 회원 정보입니다."),
    PENDING_GUARDIAN_CONSENT(HttpStatus.FORBIDDEN, "보호자 동의가 완료되지 않은 계정입니다. 보호자 동의 메일을 확인해 주세요."),

    // ===== Logout / Token =====
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "조작되었거나 만료된 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "인증 정보가 올바르지 않습니다. 다시 로그인해 주세요."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 세션입니다. 보안을 위해 다시 로그인해 주세요."),

    // ===== Password Reset =====
    INVALID_RESET_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 비밀번호 재설정 요청입니다."),
    EXPIRED_RESET_TOKEN(HttpStatus.UNAUTHORIZED, "비밀번호 재설정 링크가 만료되었습니다. 다시 요청해 주세요."),
    WEAK_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호는 영문, 숫자, 특수문자를 조합해 8자 이상이어야 합니다."),

    // ===== Signup =====
    // 이메일/닉네임/비밀번호/생년월일 형식 검증은 SignupRequestDto의 Bean Validation이 담당하며,
    // 실패 시 GlobalExceptionHandler가 공통 BAD_REQUEST 코드로 응답한다.
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "이미 가입된 이메일 주소입니다."),
    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "이미 사용 중인 닉네임입니다."),
    INVALID_BIRTH_DATE(HttpStatus.BAD_REQUEST, "생년월일이 올바르지 않습니다."),

    // ===== OAuth (소셜 로그인) =====
    UNSUPPORTED_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 로그인 제공자입니다."),
    OAUTH_AUTH_FAILED(HttpStatus.BAD_REQUEST, "소셜 로그인 인증에 실패했습니다. 다시 시도해 주세요."),
    OAUTH_EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "이메일 제공에 동의해야 가입할 수 있습니다."),
    INVALID_OAUTH_SIGNUP_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 소셜 회원가입 요청입니다."),
    EXPIRED_OAUTH_SIGNUP_TOKEN(HttpStatus.UNAUTHORIZED, "소셜 회원가입 세션이 만료되었습니다. 처음부터 다시 시도해 주세요.");

    private final HttpStatus status;
    private final String message;

    AuthErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
