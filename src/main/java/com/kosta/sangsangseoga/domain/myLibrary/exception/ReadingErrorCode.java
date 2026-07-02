package com.kosta.sangsangseoga.domain.myLibrary.exception;

import com.kosta.sangsangseoga.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ReadingErrorCode implements ErrorCode {

    // ===== ReadingMemo =====
    MEMO_NOT_FOUND(HttpStatus.NOT_FOUND, "메모를 찾을 수 없습니다."),
    MEMO_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 페이지에 메모가 존재합니다.");

    private final HttpStatus status;
    private final String message;

    ReadingErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}