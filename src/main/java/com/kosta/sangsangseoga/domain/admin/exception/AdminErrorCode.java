package com.kosta.sangsangseoga.domain.admin.exception;

import com.kosta.sangsangseoga.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AdminErrorCode implements ErrorCode {

    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "신고 정보를 찾을 수 없습니다."),
    REPORT_ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리된 신고입니다."),
    ACTION_TARGET_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "신고 대상 유형과 처리 방식이 일치하지 않습니다."),
    ACTION_TARGET_NOT_FOUND(HttpStatus.NOT_FOUND, "처리할 대상(책/댓글/작가)을 찾을 수 없습니다."),

    // ===== Member 관리 =====
    INVALID_TARGET_STATUS(HttpStatus.BAD_REQUEST, "변경할 수 없는 상태입니다. (ACTIVE/SUSPENDED/DELETED만 가능)"),
    ADMIN_STATUS_CHANGE_NOT_ALLOWED(HttpStatus.FORBIDDEN, "관리자 계정의 상태는 이 API로 변경할 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    AdminErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
