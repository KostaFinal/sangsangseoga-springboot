package com.kosta.sangsangseoga.global.common;

import lombok.Getter;

@Getter
public class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final String code;     // 비즈니스 에러 식별 코드 (성공 시 null)
    private final String message;  // 개발자 디버깅용 로그 메시지

    private ApiResponse(boolean success, T data, String code, String message) {
        this.success = success;
        this.data = data;
        this.code = code;
        this.message = message;
    }

    /**
     * 성공 응답 - 기본 메시지 사용
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, "성공");
    }

    /**
     * 성공 응답 - 디버깅용 커스텀 메시지 사용
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, null, message);
    }

    /**
     * 에러 응답 - 비즈니스 에러 코드 및 디버깅 메시지 지정
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, code, message);
    }
}