package com.kosta.sangsangseoga.domain.book.exception;

import com.kosta.sangsangseoga.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BookErrorCode implements ErrorCode {

    INVALID_BOOK_TYPE(HttpStatus.BAD_REQUEST, "bookType 값이 올바르지 않습니다 (ESSAY/POEM/FAIRY_TALE/NOVEL 중 하나여야 합니다)"),
    INVALID_SORT(HttpStatus.BAD_REQUEST, "sort 값이 올바르지 않습니다 (latest/popular/likes 중 하나여야 합니다)"),
    FREE_TRIAL_ALREADY_USED(HttpStatus.FORBIDDEN, "무료 체험(책 1권 생성)을 이미 사용했습니다. 구독 후 이용해 주세요.");

    private final HttpStatus status;
    private final String message;

    BookErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}