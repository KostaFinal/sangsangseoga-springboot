package com.kosta.sangsangseoga.global.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 * 알림 실시간 스트림(NotificationStreamListener) 전용 Redis 커넥션.
 * 기본 LettuceConnectionFactory는 커넥션을 공유하는데, 스트림 리스너는 XREAD를 poll timeout(2초) 동안
 * 블로킹하며 계속 반복하므로 같은 커넥션을 쓰면 그 사이 다른 요청의 Redis 호출(TokenBlacklistService 등)이
 * 계속 대기 행렬에 밀려 응답이 지연/멈추는 문제가 있었다. 그래서 이 리스너만 별도 커넥션을 쓰게 분리한다.
 *
 * host/port만 따로 하드코딩하지 않고 Spring Boot가 자동 바인딩한 RedisProperties를 그대로 재사용해서,
 * 나중에 password/database/ssl/timeout 등이 설정에 추가되어도 기본 커넥션과 항상 같은 값을 쓰게 한다.
 */
@Configuration
public class NotificationStreamRedisConfig {

    private final RedisProperties redisProperties;

    public NotificationStreamRedisConfig(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    @Bean
    public LettuceConnectionFactory notificationStreamConnectionFactory() {
        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration(
                redisProperties.getHost(), redisProperties.getPort());
        standaloneConfig.setDatabase(redisProperties.getDatabase());
        if (redisProperties.getUsername() != null) {
            standaloneConfig.setUsername(redisProperties.getUsername());
        }
        if (redisProperties.getPassword() != null) {
            standaloneConfig.setPassword(RedisPassword.of(redisProperties.getPassword()));
        }

        LettuceClientConfiguration.LettuceClientConfigurationBuilder clientConfigBuilder =
                LettuceClientConfiguration.builder();
        if (redisProperties.getTimeout() != null) {
            clientConfigBuilder.commandTimeout(redisProperties.getTimeout());
        }
        if (redisProperties.getConnectTimeout() != null) {
            clientConfigBuilder.clientOptions(ClientOptions.builder()
                    .socketOptions(SocketOptions.builder()
                            .connectTimeout(redisProperties.getConnectTimeout())
                            .build())
                    .build());
        }
        if (redisProperties.isSsl()) {
            clientConfigBuilder.useSsl();
        }

        return new LettuceConnectionFactory(standaloneConfig, clientConfigBuilder.build());
    }
}
