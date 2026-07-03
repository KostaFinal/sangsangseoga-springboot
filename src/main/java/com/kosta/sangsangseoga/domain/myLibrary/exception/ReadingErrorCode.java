package com.kosta.sangsangseoga.domain.myLibrary.exception;

import com.kosta.sangsangseoga.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ReadingErrorCode implements ErrorCode {

    // ===== ReadingMemo =====
    MEMO_NOT_FOUND(HttpStatus.NOT_FOUND, "메모를 찾을 수 없습니다."),
    MEMO_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 페이지에 메모가 존재합니다."),
    
 // ===== MyReading =====
    MY_READING_NOT_FOUND(HttpStatus.NOT_FOUND, "독서 정보를 찾을 수 없습니다."),
    WISHLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "읽고 싶은 책이 존재하지 않습니다."),

    // ===== ReadingPlan =====
    READING_PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "독서 계획을 찾을 수 없습니다."),
    READING_PLAN_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 등록된 독서 계획입니다."),

    // ===== BookReview =====
    BOOK_REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "독후감을 찾을 수 없습니다."),
    BOOK_REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 작성한 독후감이 있습니다.");

    private final HttpStatus status;
    private final String message;

    ReadingErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}