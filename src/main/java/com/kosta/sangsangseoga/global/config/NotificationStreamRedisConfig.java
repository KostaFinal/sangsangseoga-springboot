package com.kosta.sangsangseoga.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 * 알림 실시간 스트림(NotificationStreamListener) 전용 Redis 커넥션.
 * 기본 LettuceConnectionFactory는 커넥션을 공유하는데, 스트림 리스너는 XREAD를 poll timeout(2초) 동안
 * 블로킹하며 계속 반복하므로 같은 커넥션을 쓰면 그 사이 다른 요청의 Redis 호출(TokenBlacklistService 등)이
 * 계속 대기 행렬에 밀려 응답이 지연/멈추는 문제가 있었다. 그래서 이 리스너만 별도 커넥션을 쓰게 분리한다.
 */
@Configuration
public class NotificationStreamRedisConfig {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Bean
    public LettuceConnectionFactory notificationStreamConnectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(redisHost, redisPort));
    }
}
