package com.kosta.sangsangseoga.global.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private final JwtProperties jwtProperties;

    public String createAccessToken(Long memberId, String role) {
        return JWT.create()
                .withSubject(String.valueOf(memberId))
                .withClaim(CLAIM_ROLE, role)
                .withClaim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS)
                .withIssuedAt(new Date())
                .withExpiresAt(Date.from(Instant.now().plusMillis(jwtProperties.getAccessTokenExpiration())))
                .sign(algorithm());
    }

    public String createRefreshToken(Long memberId) {
        return createRefreshToken(memberId, jwtProperties.getRefreshTokenExpiration());
    }

    /** rememberMe 등으로 만료기간을 상황에 따라 다르게 주고 싶을 때 쓰는 오버로드. */
    public String createRefreshToken(Long memberId, long ttlMillis) {
        return JWT.create()
                .withSubject(String.valueOf(memberId))
                .withClaim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH)
                .withIssuedAt(new Date())
                .withExpiresAt(Date.from(Instant.now().plusMillis(ttlMillis)))
                .sign(algorithm());
    }

    /**
     * Access Token 전용 검증. tokenType=access가 아니면(예: Refresh Token을 그대로 전달) 실패 처리한다.
     */
    public boolean validateToken(String token) {
        try {
            JWT.require(algorithm())
                    .withClaim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS)
                    .build()
                    .verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public Long getMemberId(String token) {
        DecodedJWT decoded = JWT.require(algorithm()).build().verify(token);
        return Long.valueOf(decoded.getSubject());
    }

    public String getRole(String token) {
        DecodedJWT decoded = JWT.require(algorithm()).build().verify(token);
        return decoded.getClaim(CLAIM_ROLE).asString();
    }

    public Date getIssuedAt(String token) {
        DecodedJWT decoded = JWT.require(algorithm()).build().verify(token);
        return decoded.getIssuedAt();
    }

    /**
     * Refresh Token 전용 검증. 만료/위조를 구분해야 하는 호출부(토큰 재발급)를 위한 것이다.
     * tokenType=refresh가 아니면(예: Access Token 오용) ActionTokenInvalidException을 던진다.
     * 만료 시 ActionTokenExpiredException, 그 외 위변조 시 ActionTokenInvalidException을 던진다.
     */
    public DecodedJWT verifyRefreshToken(String token) {
        try {
            return JWT.require(algorithm())
                    .withClaim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH)
                    .build()
                    .verify(token);
        } catch (TokenExpiredException e) {
            throw new ActionTokenExpiredException();
        } catch (JWTVerificationException e) {
            throw new ActionTokenInvalidException();
        }
    }

    private Algorithm algorithm() {
        return Algorithm.HMAC256(jwtProperties.getSecretKey());
    }
}
