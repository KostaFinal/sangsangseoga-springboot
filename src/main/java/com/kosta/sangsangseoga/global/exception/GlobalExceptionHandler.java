package com.kosta.sangsangseoga.global.exception;

import com.kosta.sangsangseoga.global.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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
     * 쿼리 파라미터/경로 변수 타입 불일치 처리 (예: enum 쿼리 파라미터에 정의되지 않은 값 전달).
     * 처리하지 않으면 500(INTERNAL_SERVER_ERROR)으로 떨어져서 클라이언트 입력 오류인지 서버 오류인지
     * 구분이 안 되므로, 400으로 변환해 클라이언트에게 잘못된 요청임을 알려준다.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String message = String.format("'%s' 파라미터의 값 '%s'이(가) 올바르지 않습니다.", e.getName(), e.getValue());
        log.warn("파라미터 타입 불일치: {}", message);
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
     * 낙관적 락(@Version) 충돌 처리. 같은 회원 row를 동시에 읽고 쓰는 요청(API 중복 호출,
     * 배치와 API 동시 실행 등)이 겹쳤을 때 나중에 커밋을 시도한 쪽이 여기서 걸린다.
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLockException(ObjectOptimisticLockingFailureException e) {
        log.warn("낙관적 락 충돌 발생: {}", e.getMessage());
        ErrorCode errorCode = CommonErrorCode.CONCURRENT_UPDATE_CONFLICT;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode.name(), errorCode.getMessage()));
    }

    /**
     * 클라이언트가 응답을 받기 전에 연결을 끊은 경우(브라우저 뒤로가기/새로고침/페이지 이탈,
     * 소셜 로그인 팝업 중도 취소 등). 서버가 실제로 처리에 실패한 게 아니라 상대방이 이미
     * 사라진 것뿐이므로, 잘못된 요청 취급하며 응답 바디를 쓰려 하지 않는다(써봐야 또 실패한다).
     * ERROR 로그로 스택트레이스를 남기지도 않는다 - 운영상 흔히 발생하는 정상적인 상황이라
     * 매번 에러 로그가 쌓이면 진짜 장애 신호를 파묻어 버린다.
     */
    @ExceptionHandler(ClientAbortException.class)
    public void handleClientAbortException(ClientAbortException e) {
        log.debug("클라이언트가 응답을 받기 전에 연결을 끊었습니다: {}", e.getMessage());
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