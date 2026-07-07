package com.kosta.sangsangseoga.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kosta.sangsangseoga.global.common.ApiResponse;
import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * hasRole 등으로 보호된 경로에 로그인 없이(또는 유효하지 않은 토큰으로) 접근했을 때의 401 응답을
 * 다른 API들과 동일한 ApiResponse 포맷으로 내려준다. 기본 Spring Security 응답은 이 포맷을 따르지 않는다.
 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException {
        CommonErrorCode errorCode = CommonErrorCode.UNAUTHORIZED;
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(ApiResponse.error(errorCode.name(), errorCode.getMessage())));
    }
}
