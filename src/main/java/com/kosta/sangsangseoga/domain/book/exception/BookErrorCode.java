package com.kosta.sangsangseoga.domain.book.exception;

import com.kosta.sangsangseoga.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BookErrorCode implements ErrorCode {

    INVALID_BOOK_TYPE(HttpStatus.BAD_REQUEST, "bookType 값이 올바르지 않습니다 (ESSAY/POEM/NONFICTION/FAIRY_TALE/NOVEL 중 하나여야 합니다)"),
    INVALID_SORT(HttpStatus.BAD_REQUEST, "sort 값이 올바르지 않습니다 (latest/popular/likes 중 하나여야 합니다)");

    private final HttpStatus status;
    private final String message;

    BookErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}