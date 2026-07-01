package com.kosta.sangsangseoga.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // ===== 공통에러 =========
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다. 다시 확인해 주세요."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인 세션이 만료되었습니다. 다시 로그인해 주세요."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "이 작업을 수행할 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청하신 정보를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
    // =======================

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}