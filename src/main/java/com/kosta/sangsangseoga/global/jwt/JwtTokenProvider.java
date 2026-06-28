package com.kosta.sangsangseoga.global.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final JwtProperties jwtProperties;

    public String createToken(String email) {
        return "mock-jwt-token-for-" + email;
    }

    public boolean validateToken(String token) {
        return true;
    }

    public String getEmail(String token) {
        return "user@example.com";
    }
}
