package com.kosta.sangsangseoga.global.exception;

import com.kosta.sangsangseoga.global.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

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
     * @Valid로 선언한 Bean Validation(DTO 필드 애너테이션) 실패 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("입력값 검증 실패: {}", message);
        return ResponseEntity
                .status(CommonErrorCode.BAD_REQUEST.getStatus())
                .body(ApiResponse.error(CommonErrorCode.BAD_REQUEST.name(), message));
    }

    /**
     * 지원하지 않는 HTTP 메서드로 요청한 경우 (예: POST 전용 API에 GET으로 접근)
     * 브라우저 주소창 직접 접근, Swagger "Try it out" 오용 등에서 흔히 발생한다.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e) {
        log.warn("지원하지 않는 HTTP 메서드 요청: {} (지원 메서드: {})", e.getMethod(), e.getSupportedHttpMethods());
        ErrorCode errorCode = CommonErrorCode.METHOD_NOT_ALLOWED;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode.name(), errorCode.getMessage()));
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