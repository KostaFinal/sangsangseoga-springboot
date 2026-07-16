package com.kosta.sangsangseoga.global.security;

import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * SecurityConfig가 "/**"를 permitAll로 열어두고 있어, 토큰 없이 호출하면 JwtAuthFilter가
 * SecurityContext를 채우지 않고 Spring Security의 익명 인증(principal이 "anonymousUser" 문자열)이
 * 대신 채워진다. 이 상태에서 (Long) authentication.getPrincipal()로 바로 캐스팅하면
 * ClassCastException이 GlobalExceptionHandler의 500 처리로 떨어져 버그처럼 보인다.
 * 로그인이 필요한 컨트롤러 메서드는 캐스팅 대신 이 메서드로 회원 ID를 꺼내 401로 응답하게 한다.
 */
public final class AuthenticationHelper {

    private AuthenticationHelper() {
    }

    public static Long resolveMemberId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Long)) {
            throw new CustomException(CommonErrorCode.UNAUTHORIZED);
        }
        return (Long) authentication.getPrincipal();
    }

    /** JwtAuthFilter가 "ROLE_"를 붙여 부여한 권한에서 원래 role 문자열만 꺼낸다(예: SSE 티켓 발급용). */
    public static String resolveRole(Authentication authentication) {
        if (authentication == null) {
            throw new CustomException(CommonErrorCode.UNAUTHORIZED);
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .findFirst()
                .orElseThrow(() -> new CustomException(CommonErrorCode.UNAUTHORIZED));
    }
}
