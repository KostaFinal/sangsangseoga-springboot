package com.kosta.sangsangseoga.domain.member.exception;

import com.kosta.sangsangseoga.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MemberErrorCode implements ErrorCode {

    // ===== Withdrawal =====
    WRONG_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    ALREADY_DELETED_MEMBER(HttpStatus.NOT_FOUND, "이미 탈퇴 처리가 완료된 회원입니다."),

    // ===== GuardianConsent =====
    ALREADY_APPROVED_MEMBER(HttpStatus.BAD_REQUEST, "이미 보호자 동의 완료가 처리된 계정입니다."),
    GUARDIAN_CONSENT_NOT_FOUND(HttpStatus.NOT_FOUND, "동의 요청 정보를 찾을 수 없습니다."),
    GUARDIAN_CONSENT_ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리된 동의 요청입니다."),
    GUARDIAN_CONSENT_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 동의 요청입니다. 다시 요청해 주세요."),
    INVALID_CONSENT_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 동의 인증 정보입니다.");

    private final HttpStatus status;
    private final String message;

    MemberErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
