package com.kosta.sangsangseoga.domain.auth.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.kosta.sangsangseoga.domain.auth.dto.OAuthAuthorizeUrlResponseDto;
import com.kosta.sangsangseoga.domain.auth.dto.OAuthCallbackRequestDto;
import com.kosta.sangsangseoga.domain.auth.dto.OAuthCallbackResponseDto;
import com.kosta.sangsangseoga.domain.auth.dto.OAuthCompleteSignupRequestDto;
import com.kosta.sangsangseoga.domain.auth.exception.AuthErrorCode;
import com.kosta.sangsangseoga.domain.auth.oauth.OAuthClientResolver;
import com.kosta.sangsangseoga.domain.auth.oauth.OAuthUserInfo;
import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.enums.AuthProvider;
import com.kosta.sangsangseoga.domain.member.enums.MemberAgeGroup;
import com.kosta.sangsangseoga.domain.member.enums.MemberStatus;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
import com.kosta.sangsangseoga.global.event.AfterCommitTask;
import com.kosta.sangsangseoga.global.exception.CustomException;
import com.kosta.sangsangseoga.global.jwt.ActionTokenExpiredException;
import com.kosta.sangsangseoga.global.jwt.ActionTokenInvalidException;
import com.kosta.sangsangseoga.global.jwt.ActionTokenProvider;
import com.kosta.sangsangseoga.global.jwt.JwtTokenProvider;
import com.kosta.sangsangseoga.global.jwt.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 카카오/네이버 소셜 로그인. 인가 URL 생성, 토큰 교환, 2단계 가입처럼 이메일 로그인(AuthService)에는
 * 없는 흐름이 대부분이라 별개 서비스로 분리했다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OAuthService {

    private static final String OAUTH_SIGNUP_TOKEN_PURPOSE = "OAUTH_SIGNUP";
    private static final long OAUTH_SIGNUP_TOKEN_TTL_MILLIS = 30L * 60 * 1000; // 30분
    private static final int MINOR_U14_AGE_LIMIT = 14;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final MemberRepository memberRepository;
    private final OAuthClientResolver oAuthClientResolver;
    private final ActionTokenProvider actionTokenProvider;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    public OAuthAuthorizeUrlResponseDto getAuthorizeUrl(String providerName, String redirectUri) {
        AuthProvider provider = oAuthClientResolver.resolveProvider(providerName);
        String url = oAuthClientResolver.resolve(provider).buildAuthorizeUrl(redirectUri);
        return OAuthAuthorizeUrlResponseDto.builder().authorizeUrl(url).build();
    }

    /**
     * 로그인/가입 겸용 콜백. 기존 회원이면 로그인, 신규면 생년월일 제공 여부에 따라 가입을 바로
     * 끝내거나(성인/미성년 분기) complete-signup을 한 번 더 받는다(oauthSignupToken 발급).
     */
    public OAuthCallbackResponseDto handleCallback(String providerName, OAuthCallbackRequestDto request) {
        AuthProvider provider = oAuthClientResolver.resolveProvider(providerName);
        OAuthUserInfo userInfo = oAuthClientResolver.resolve(provider).fetchUserInfo(request.getCode(), request.getRedirectUri());

        if (userInfo.getEmail() == null || userInfo.getEmail().isBlank()) {
            throw new CustomException(AuthErrorCode.OAUTH_EMAIL_REQUIRED);
        }

        Optional<Member> existingMember = memberRepository.findByAuthProviderAndOauthProviderId(provider, userInfo.getProviderId());
        if (existingMember.isPresent()) {
            return loginExisting(existingMember.get());
        }

        if (userInfo.getBirthDate() == null) {
            return issueSignupToken(provider, userInfo);
        }

        String nickname = resolveUniqueNickname(sanitizeNickname(userInfo.getNickname()));
        return signup(provider, userInfo.getProviderId(), userInfo.getEmail(), nickname,
                userInfo.getProfileImageUrl(), userInfo.getBirthDate());
    }

    /**
     * 콜백에서 생년월일을 못 받았을 때만 호출되는 2단계 가입 완료. oauthSignupToken에 담아뒀던
     * provider/providerId/email/profileImageUrl과, 이번에 사용자가 직접 입력한 nickname/birthDate로 가입을 끝낸다.
     */
    public OAuthCallbackResponseDto completeSignup(OAuthCompleteSignupRequestDto request) {
        DecodedJWT decoded;
        try {
            decoded = actionTokenProvider.verify(request.getOauthSignupToken(), OAUTH_SIGNUP_TOKEN_PURPOSE);
        } catch (ActionTokenExpiredException e) {
            throw new CustomException(AuthErrorCode.EXPIRED_OAUTH_SIGNUP_TOKEN);
        } catch (ActionTokenInvalidException e) {
            throw new CustomException(AuthErrorCode.INVALID_OAUTH_SIGNUP_TOKEN);
        }

        AuthProvider provider = AuthProvider.valueOf(decoded.getClaim("provider").asString());
        String providerId = decoded.getClaim("providerId").asString();
        String email = decoded.getClaim("email").asString();
        String profileImageUrl = decoded.getClaim("profileImageUrl").asString();

        if (memberRepository.findByAuthProviderAndOauthProviderId(provider, providerId).isPresent()) {
            // 같은 토큰으로 이미 가입이 완료된 상태(재시도/중복 제출)
            throw new CustomException(AuthErrorCode.INVALID_OAUTH_SIGNUP_TOKEN);
        }

        try {
            actionTokenProvider.consume(decoded);
        } catch (ActionTokenInvalidException e) {
            throw new CustomException(AuthErrorCode.INVALID_OAUTH_SIGNUP_TOKEN);
        }

        return signup(provider, providerId, email, request.getNickname(),
                profileImageUrl == null || profileImageUrl.isBlank() ? null : profileImageUrl,
                request.getBirthDate());
    }

    private OAuthCallbackResponseDto loginExisting(Member member) {
        // /api/auth/login과 동일한 상태 게이트. PENDING(보호자 동의 대기)으로 만들어진 소셜 계정도
        // 이메일 가입 계정과 똑같이 여기서 막힌다 - 별도 분기를 두지 않고 login()과 같은 검사를 그대로 적용한다.
        if (member.getStatus() == MemberStatus.SUSPENDED) {
            throw new CustomException(AuthErrorCode.SUSPENDED_MEMBER);
        }
        if (member.getStatus() == MemberStatus.DELETED) {
            throw new CustomException(AuthErrorCode.DELETED_MEMBER);
        }
        if (member.getStatus() == MemberStatus.PENDING) {
            throw new CustomException(AuthErrorCode.PENDING_GUARDIAN_CONSENT);
        }
        return issueTokens(member);
    }

    private OAuthCallbackResponseDto issueSignupToken(AuthProvider provider, OAuthUserInfo userInfo) {
        Map<String, String> claims = new HashMap<>();
        claims.put("provider", provider.name());
        claims.put("providerId", userInfo.getProviderId());
        claims.put("email", userInfo.getEmail());
        claims.put("profileImageUrl", userInfo.getProfileImageUrl() == null ? "" : userInfo.getProfileImageUrl());

        String token = actionTokenProvider.create(OAUTH_SIGNUP_TOKEN_PURPOSE, 0L, OAUTH_SIGNUP_TOKEN_TTL_MILLIS, claims);

        return OAuthCallbackResponseDto.builder()
                .isNewMember(true)
                .oauthSignupToken(token)
                .email(userInfo.getEmail())
                .nickname(userInfo.getNickname())
                .profileImageUrl(userInfo.getProfileImageUrl())
                .build();
    }

    private OAuthCallbackResponseDto signup(AuthProvider provider, String providerId, String email, String nickname,
                                             String profileImageUrl, LocalDate birthDate) {
        if (memberRepository.existsByEmail(email)) {
            throw new CustomException(AuthErrorCode.DUPLICATE_EMAIL);
        }
        if (memberRepository.existsByNickname(nickname)) {
            throw new CustomException(AuthErrorCode.DUPLICATE_NICKNAME);
        }

        MemberStatus initialStatus = resolveInitialStatus(birthDate);

        // 소셜 계정은 비밀번호로 로그인할 일이 없다 - 그래도 컬럼은 NOT NULL이라, 알 수 없는 값으로
        // 채워서 이메일/비밀번호 로그인 시도가 "그냥 비밀번호 틀림"으로 자연스럽게 막히게 한다.
        Member member = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .birthDate(birthDate)
                .status(initialStatus)
                .authProvider(provider)
                .oauthProviderId(providerId)
                .build();
        memberRepository.save(member);

        if (initialStatus == MemberStatus.PENDING) {
            return OAuthCallbackResponseDto.builder()
                    .pendingGuardianConsent(true)
                    .memberId(member.getId())
                    .build();
        }
        return issueTokens(member);
    }

    private OAuthCallbackResponseDto issueTokens(Member member) {
        String accessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());
        Long memberId = member.getId();
        eventPublisher.publishEvent(new AfterCommitTask(this, () -> refreshTokenService.save(memberId, refreshToken)));

        return OAuthCallbackResponseDto.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImageUrl())
                .role(member.getRole().name())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /** 콜백에서 제공자가 준 닉네임 그대로 자동 가입시키는 경로 전용 - 사용자가 직접 고칠 기회가 없어 겹치면 임의 숫자를 붙인다. */
    private String resolveUniqueNickname(String baseNickname) {
        if (!memberRepository.existsByNickname(baseNickname)) {
            return baseNickname;
        }
        for (int i = 0; i < 5; i++) {
            String candidate = baseNickname + (1000 + RANDOM.nextInt(9000));
            if (candidate.length() <= 10 && !memberRepository.existsByNickname(candidate)) {
                return candidate;
            }
        }
        throw new CustomException(AuthErrorCode.DUPLICATE_NICKNAME);
    }

    /** 프로젝트 닉네임 규칙(특수문자 제외 2~10자)에 맞게 소셜 프로필 닉네임을 다듬는다. */
    private String sanitizeNickname(String rawNickname) {
        String cleaned = rawNickname == null ? "" : rawNickname.replaceAll("[^0-9A-Za-z가-힣]", "");
        if (cleaned.length() > 10) {
            cleaned = cleaned.substring(0, 10);
        }
        return cleaned.length() >= 2 ? cleaned : "회원" + (1000 + RANDOM.nextInt(9000));
    }

    private MemberStatus resolveInitialStatus(LocalDate birthDate) {
        return calculateAgeGroup(birthDate) == MemberAgeGroup.MINOR_U14 ? MemberStatus.PENDING : MemberStatus.ACTIVE;
    }

    private MemberAgeGroup calculateAgeGroup(LocalDate birthDate) {
        if (birthDate.isAfter(LocalDate.now())) {
            throw new CustomException(AuthErrorCode.INVALID_BIRTH_DATE);
        }
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < MINOR_U14_AGE_LIMIT) {
            return MemberAgeGroup.MINOR_U14;
        }
        return age < 19 ? MemberAgeGroup.MINOR : MemberAgeGroup.ADULT;
    }
}
