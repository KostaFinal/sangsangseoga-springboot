package com.kosta.sangsangseoga.global.config;

import com.kosta.sangsangseoga.global.jwt.JwtAuthFilter;
import com.kosta.sangsangseoga.global.jwt.JwtTokenProvider;
import com.kosta.sangsangseoga.global.jwt.TokenBlacklistService;
import com.kosta.sangsangseoga.global.security.RestAccessDeniedHandler;
import com.kosta.sangsangseoga.global.security.RestAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 쿠키를 쓰지 않고 Authorization 헤더로만 인증하므로 모든 출처를 허용해도 안전하다.
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .cors().configurationSource(corsConfigurationSource())
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            // 관리자 전용 경로만 먼저 명시적으로 막는다. Spring Security는 먼저 매칭되는 규칙을 쓰므로
            // 아래 "/**" permitAll보다 반드시 앞에 있어야 한다.
            .antMatchers("/api/admin/**").hasRole("ADMIN")
            .antMatchers("/**").permitAll()
            .anyRequest().authenticated()
            .and()
            .exceptionHandling()
            .authenticationEntryPoint(restAuthenticationEntryPoint)
            .accessDeniedHandler(restAccessDeniedHandler)
            .and()
            // 그 외 경로별 인증 강제 여부는 아직 permitAll 상태이며, 이 필터는 토큰이 있을 때 SecurityContext에
            // 회원 신원(ID·권한)만 채워 넣는다. 엔드포인트별 인가 정책은 별도로 정리가 필요하다.
            .addFilterBefore(new JwtAuthFilter(jwtTokenProvider, tokenBlacklistService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
