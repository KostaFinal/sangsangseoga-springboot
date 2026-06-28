
# Spring Boot JPA 엔티티 및 글로벌 상세 정의서 (v4 - 상상서가 학습형 버전)

> 본 문서는 Claude Code가 `src/main/java/com/kosta/sangsangseoga/` 내부 코드를 완성하기 위한 가이드라인입니다.
> 팀원들의 학습을 위해 엔티티는 핵심 애노테이션과 PK(Id)만 생성하며, `global` 영역은 완전한 코드로 구현합니다.

---

## 1. 생성 규칙 (Claude Code 필독)

1. **Java 11 스펙 준수**: `record` 문법을 절대 사용하지 않습니다.
2. **글로벌 영역 (`global/`)**: `infra` 패키지를 제외한 모든 클래스(`config`, `exception`, `common`, `jwt`)는 실제 작동 가능한 **완전한 소스 코드**까지 구현합니다.
3. **엔티티 영역 (`entity/`)**: 팀원들이 직접 필드를 작성하며 익힐 수 있도록, 오직 **클래스명 위 애노테이션, `extends BaseEntity`, 그리고 `@Id` 필드**까지만 작성하고 내부 필드는 비워둡니다.
4. **간접 참조(ID 참조) 원칙**: 엔티티 간 직접 연관관계 매핑(`@ManyToOne` 등)은 제외하고 뼈대만 만듭니다.

---

## 2. 글로벌 영역 상세 구현 명세 (완전한 코드 작성)

### 📁 global/common

#### `BaseEntity.java`
```java
package com.kosta.sangsangseoga.global.common;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.Column;
import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

```

#### `ApiResponse.java`

```java
package com.kosta.sangsangseoga.global.common;

import lombok.Getter;

@Getter
public class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final String message;

    private ApiResponse(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, "성공");
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message);
    }
}

```

### 📁 global/config

#### `FileStorageConfig.java`

```java
package com.kosta.sangsangseoga.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileStorageConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}

```

#### `SecurityConfig.java`

```java
package com.kosta.sangsangseoga.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers("/**").permitAll()
            .anyRequest().authenticated();
        return http.build();
    }
}

```

#### `SwaggerConfig.java`

```java
package com.kosta.sangsangseoga.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.OAS_30)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.kosta.sangsangseoga"))
                .paths(PathSelectors.any())
                .build();
    }
}

```

#### `WebConfig.java`

```java
package com.kosta.sangsangseoga.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*");
    }
}

```

#### `JpaAuditingConfig.java`

```java
package com.kosta.sangsangseoga.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}

```

### 📁 global/exception

#### `CustomException.java`

```java
package com.kosta.sangsangseoga.global.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

```

#### `ErrorCode.java`

```java
package com.kosta.sangsangseoga.global.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    INVALID_INPUT_VALUE(400, "잘못된 입력값입니다."),
    MEMBER_NOT_FOUND(404, "회원을 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(500, "서버 내부 오류가 발생했습니다.");

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}

```

#### `GlobalExceptionHandler.java`

```java
package com.kosta.sangsangseoga.global.exception;

import com.kosta.sangsangseoga.global.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        return ResponseEntity
                .status(500)
                .body(ApiResponse.error(e.getMessage()));
    }
}

```

### 📁 global/jwt

#### `JwtProperties.java`

```java
package com.kosta.sangsangseoga.global.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secretKey = "defaultSecretKeySecretKeySecretKeySecretKeySecretKey";
    private long accessTokenExpiration = 3600000; // 1시간
}

```

#### `JwtTokenProvider.java`

```java
package com.kosta.sangsangseoga.global.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Date;

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

```

#### `JwtAuthFilter.java`

```java
package com.kosta.sangsangseoga.global.jwt;

import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        filterChain.doFilter(request, response);
    }
}

```

---

## 3. 도메인별 엔티티 학습용 뼈대 정의 (33개 파일 공통 표준)

Claude Code는 아래 명시된 모든 엔티티들을 필드 없이 **애노테이션, `extends BaseEntity`, `@Id` 필드만 포함된 표준 상태**로 구현합니다. 테이블 이름은 스네이크 케이스(`name = "테이블명"`) 규칙을 엄격히 적용합니다.

### 📁 domain/account/entity

* `Member.java` (@Table(name = "member"))
* `GuardianConsent.java` (@Table(name = "guardian_consent"))
* `GuardianLink.java` (@Table(name = "guardian_link"))
* `MemberWithdrawal.java` (@Table(name = "member_withdrawal"))

### 📁 domain/subscription/entity

* `Subscription.java` (@Table(name = "subscription"))
* `Payment.java` (@Table(name = "payment"))

### 📁 domain/editor/entity

* `BookProject.java` (@Table(name = "book_project"))
* `BookProjectPage.java` (@Table(name = "book_project_page"))
* `FairyTaleSetting.java` (@Table(name = "fairy_tale_setting"))
* `NovelSetting.java` (@Table(name = "novel_setting"))
* `EssaySetting.java` (@Table(name = "essay_setting"))
* `PoemSetting.java` (@Table(name = "poem_setting"))
* `NonfictionSetting.java` (@Table(name = "nonfiction_setting"))

### 📁 domain/prompt/entity

* `ImageGenerationRequest.java` (@Table(name = "image_generation_request"))
* `BookImage.java` (@Table(name = "book_image"))
* `AiGenerationUsage.java` (@Table(name = "ai_generation_usage"))
* `DailyTokenUsageSnapshot.java` (@Table(name = "daily_token_usage_snapshot"))

### 📁 domain/community/entity

* `Book.java` (@Table(name = "book"))
* `BookPage.java` (@Table(name = "book_page"))
* `Comment.java` (@Table(name = "comment"))
* `BookReview.java` (@Table(name = "book_review"))
* `AiReviewFeedback.java` (@Table(name = "ai_review_feedback"))
* `BookLike.java` (@Table(name = "book_like"))
* `AuthorFollow.java` (@Table(name = "author_follow"))
* `Report.java` (@Table(name = "report"))

### 📁 domain/mylibrary/entity

* `UserBook.java` (@Table(name = "user_book"))
* `ReadingProgress.java` (@Table(name = "reading_progress"))
* `LibraryRanking.java` (@Table(name = "library_ranking"))

### 📁 domain/viewer/entity

* `ViewerSetting.java` (@Table(name = "viewer_setting"))
* `BookBookmark.java` (@Table(name = "book_bookmark"))

### 📁 domain/admin/entity

* `AdminActionLog.java` (@Table(name = "admin_action_log"))
* `RecommendedBook.java` (@Table(name = "recommended_book"))
* `WeeklyBookStat.java` (@Table(name = "weekly_book_stat"))

### 💡 엔티티 코드 작성을 위한 공통 템플릿 표준 양식 (예시)

Claude Code는 위 33개 엔티티 파일들을 아래 구조를 기본으로 하여, 각각의 클래스명과 `@Table(name)`만 치환하여 소스 코드를 생성합니다.

```java
package com.kosta.sangsangseoga.domain.account.entity;

import com.kosta.sangsangseoga.global.common.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}

```

```
