package com.kosta.sangsangseoga.global.exception;

import com.kosta.sangsangseoga.global.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직 예외 처리 (CustomException)
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("CustomException 발생: {} - {}", errorCode.name(), e.getMessage());
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode.name(), e.getMessage()));
    }

    /**
     * 그 외 예측하지 못한 서버 내부 시스템 예외 처리 (NullPointerException, DB 에러 등)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("에러 발생: ", e);
        ErrorCode serverError = CommonErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(serverError.getStatus())
                .body(ApiResponse.error(serverError.name(), serverError.getMessage()));
    }
}