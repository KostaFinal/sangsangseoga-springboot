package com.kosta.sangsangseoga.global.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Authorization: Bearer <AccessToken> 헤더를 검증해 SecurityContext에 인증 정보(회원 ID, 권한)를 채워 넣는다.
 * 어떤 경로에 인증을 강제할지(인가 정책)는 SecurityConfig가 결정하며, 이 필터는 토큰이 있을 때 신원만 채운다.
 */
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String SSE_NOTIFICATION_STREAM_PATH = "/api/notifications/stream";

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            Long memberId = jwtTokenProvider.getMemberId(token);
            Date issuedAt = jwtTokenProvider.getIssuedAt(token);

            // 정지/탈퇴 등으로 발급 시점 이후 상태가 바뀐 회원의 구 토큰은 만료 전이어도 여기서 걸러낸다.
            if (!tokenBlacklistService.isInvalidated(memberId, issuedAt)) {
                String role = jwtTokenProvider.getRole(token);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        memberId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER_AUTHORIZATION);
        if (header != null && header.startsWith(TOKEN_PREFIX)) {
            return header.substring(TOKEN_PREFIX.length());
        }
        // EventSource는 커스텀 헤더를 못 보내므로, 알림 실시간 구독 엔드포인트만 예외적으로 쿼리 파라미터를 허용한다.
        if (SSE_NOTIFICATION_STREAM_PATH.equals(request.getRequestURI())) {
            return request.getParameter("token");
        }
        return null;
    }
}
