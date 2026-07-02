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

    private final JwtProperties jwtProperties;

    public String createAccessToken(Long memberId, String role) {
        return JWT.create()
                .withSubject(String.valueOf(memberId))
                .withClaim(CLAIM_ROLE, role)
                .withIssuedAt(new Date())
                .withExpiresAt(Date.from(Instant.now().plusMillis(jwtProperties.getAccessTokenExpiration())))
                .sign(algorithm());
    }

    public String createRefreshToken(Long memberId) {
        return JWT.create()
                .withSubject(String.valueOf(memberId))
                .withIssuedAt(new Date())
                .withExpiresAt(Date.from(Instant.now().plusMillis(jwtProperties.getRefreshTokenExpiration())))
                .sign(algorithm());
    }

    public boolean validateToken(String token) {
        try {
            JWT.require(algorithm()).build().verify(token);
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

    /**
     * 만료/위조를 구분해야 하는 호출부(토큰 재발급 등)를 위한 검증.
     * 만료 시 ActionTokenExpiredException, 그 외 위변조 시 ActionTokenInvalidException을 던진다.
     */
    public DecodedJWT verify(String token) {
        try {
            return JWT.require(algorithm()).build().verify(token);
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
