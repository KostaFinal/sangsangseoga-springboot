package com.kosta.sangsangseoga.global.exception;
 
import org.springframework.http.HttpStatus;

import lombok.Getter;
 
@Getter
public enum CommonErrorCode implements ErrorCode {
 
    // ===== 공통 에러 =====
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다. 다시 확인해 주세요."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인 세션이 만료되었습니다. 다시 로그인해 주세요."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "이 작업을 수행할 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청하신 정보를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."),
 
    // ===== Member =====
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
 
    // ===== Book =====
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "책을 찾을 수 없습니다.");
	
    private final HttpStatus status;
    private final String message;
 
    CommonErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}