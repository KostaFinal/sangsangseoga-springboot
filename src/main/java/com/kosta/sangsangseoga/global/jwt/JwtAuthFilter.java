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
 *
 * 알림 실시간 구독 엔드포인트(SSE_NOTIFICATION_STREAM_PATH)만 예외적으로 다른 인증 방식을 쓴다: EventSource는
 * 커스텀 헤더를 못 보내는데, 그렇다고 수명이 긴 JWT를 쿼리 파라미터에 그대로 실으면 로그/리퍼러/브라우저
 * 히스토리로 새어나갈 수 있다. 그래서 짧은 TTL(30초)의 1회용 티켓(SseTicketService)만 쿼리 파라미터로 받고,
 * 일반 JWT는 이 경로에서도 쿼리 파라미터로는 절대 받지 않는다.
 */
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String SSE_NOTIFICATION_STREAM_PATH = "/api/notifications/stream";

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final SseTicketService sseTicketService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (isSseTicketRequest(request)) {
            authenticateViaSseTicket(request);
        } else {
            authenticateViaJwt(request);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isSseTicketRequest(HttpServletRequest request) {
        return SSE_NOTIFICATION_STREAM_PATH.equals(request.getRequestURI());
    }

    private void authenticateViaSseTicket(HttpServletRequest request) {
        SseTicketService.Ticket ticket = sseTicketService.consume(request.getParameter("ticket"));
        if (ticket == null) {
            return;
        }
        setAuthentication(ticket.getMemberId(), ticket.getRole());
    }

    private void authenticateViaJwt(HttpServletRequest request) {
        String token = resolveToken(request);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return;
        }

        Long memberId = jwtTokenProvider.getMemberId(token);
        Date issuedAt = jwtTokenProvider.getIssuedAt(token);

        // 정지/탈퇴 등으로 발급 시점 이후 상태가 바뀐 회원의 구 토큰은 만료 전이어도 여기서 걸러낸다.
        if (!tokenBlacklistService.isInvalidated(memberId, issuedAt)) {
            setAuthentication(memberId, jwtTokenProvider.getRole(token));
        }
    }

    private void setAuthentication(Long memberId, String role) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                memberId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER_AUTHORIZATION);
        if (header != null && header.startsWith(TOKEN_PREFIX)) {
            return header.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
