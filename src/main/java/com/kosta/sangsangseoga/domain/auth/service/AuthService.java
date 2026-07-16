package com.kosta.sangsangseoga.domain.auth.service;


import java.time.LocalDate;
import java.time.Period;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.kosta.sangsangseoga.domain.auth.dto.LoginRequestDto;
import com.kosta.sangsangseoga.domain.auth.dto.LoginResponseDto;
import com.kosta.sangsangseoga.domain.auth.dto.PasswordResetCompleteDto;
import com.kosta.sangsangseoga.domain.auth.dto.PasswordResetRequestDto;
import com.kosta.sangsangseoga.domain.auth.dto.SignupRequestDto;
import com.kosta.sangsangseoga.domain.auth.dto.SignupResponseDto;
import com.kosta.sangsangseoga.domain.auth.dto.TokenRefreshRequestDto;
import com.kosta.sangsangseoga.domain.auth.dto.TokenRefreshResponseDto;
import com.kosta.sangsangseoga.domain.auth.exception.AuthErrorCode;
import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.enums.AuthProvider;
import com.kosta.sangsangseoga.domain.member.enums.MemberAgeGroup;
import com.kosta.sangsangseoga.domain.member.enums.MemberStatus;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
import com.kosta.sangsangseoga.global.event.AfterCommitTask;
import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;
import com.kosta.sangsangseoga.global.jwt.ActionTokenExpiredException;
import com.kosta.sangsangseoga.global.jwt.ActionTokenInvalidException;
import com.kosta.sangsangseoga.global.jwt.ActionTokenProvider;
import com.kosta.sangsangseoga.global.jwt.JwtProperties;
import com.kosta.sangsangseoga.global.jwt.JwtTokenProvider;
import com.kosta.sangsangseoga.global.jwt.RefreshTokenService;
import com.kosta.sangsangseoga.global.mail.MailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private static final String PASSWORD_RESET_TOKEN_PURPOSE = "PASSWORD_RESET";
    private static final long PASSWORD_RESET_TOKEN_TTL_MILLIS = 30 * 60 * 1000L; // 30분
    private static final int MINOR_U14_AGE_LIMIT = 14;
    private static final int ADULT_AGE = 19;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActionTokenProvider actionTokenProvider;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;
    private final ApplicationEventPublisher eventPublisher;
    private final MailService mailService;

    /**
     * 회원가입. 형식 검증은 SignupRequestDto가 담당하고, 여기서는 DB 조회가 필요한 중복 검증과 가입을 처리한다.
     * 만 14세 미만은 PENDING 상태로 가입하고 토큰을 발급하지 않는다 — 발급하면 보호자 동의 없이도 그 토큰으로
     * API를 호출할 수 있어 게이트가 무력화된다. 그 외는 즉시 ACTIVE로 가입하고 Access/Refresh Token을 발급한다.
     * 소셜 가입(OAuthService.signup)과 동일한 규칙이다.
     */
    public SignupResponseDto signup(SignupRequestDto request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(AuthErrorCode.DUPLICATE_EMAIL);
        }
        if (memberRepository.existsByNickname(request.getNickname())) {
            throw new CustomException(AuthErrorCode.DUPLICATE_NICKNAME);
        }

        MemberStatus initialStatus = resolveInitialStatus(request.getBirthDate());

        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .profileImageUrl(request.getProfileImageUrl())
                .birthDate(request.getBirthDate())
                .status(initialStatus)
                .build();
        memberRepository.save(member);

        if (initialStatus == MemberStatus.PENDING) {
            return SignupResponseDto.builder()
                    .memberId(member.getId())
                    .email(member.getEmail())
                    .nickname(member.getNickname())
                    .role(member.getRole().name())
                    .pendingGuardianConsent(true)
                    .build();
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());
        Long memberId = member.getId();
        eventPublisher.publishEvent(new AfterCommitTask(this, () -> refreshTokenService.save(memberId, refreshToken)));

        return SignupResponseDto.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .role(member.getRole().name())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private MemberStatus resolveInitialStatus(LocalDate birthDate) {
        MemberAgeGroup ageGroup = calculateAgeGroup(birthDate);
        return ageGroup == MemberAgeGroup.MINOR_U14 ? MemberStatus.PENDING : MemberStatus.ACTIVE;
    }

    private MemberAgeGroup calculateAgeGroup(LocalDate birthDate) {
        if (birthDate.isAfter(LocalDate.now())) {
            throw new CustomException(AuthErrorCode.INVALID_BIRTH_DATE);
        }
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < MINOR_U14_AGE_LIMIT) {
            return MemberAgeGroup.MINOR_U14;
        }
        if (age < ADULT_AGE) {
            return MemberAgeGroup.MINOR;
        }
        return MemberAgeGroup.ADULT;
    }

    private void validatePassword(String password) {
        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new CustomException(AuthErrorCode.WEAK_PASSWORD);
        }
    }

    /**
     * 이메일/비밀번호 로그인. Access/Refresh Token을 동시 발급하고,
     * Refresh Token은 회원 ID를 키로 Redis 화이트리스트에 저장한다.
     * rememberMe=true면 Refresh Token을 기본 만료기간(30일)으로, false/미지정이면 짧은 만료기간(1일)으로 발급한다.
     */
    public LoginResponseDto login(LoginRequestDto request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(AuthErrorCode.LOGIN_FAILED));

        if (member.getStatus() == MemberStatus.SUSPENDED) {
            throw new CustomException(AuthErrorCode.SUSPENDED_MEMBER);
        }
        if (member.getStatus() == MemberStatus.DELETED) {
            throw new CustomException(AuthErrorCode.DELETED_MEMBER);
        }
        if (member.getStatus() == MemberStatus.PENDING) {
            throw new CustomException(AuthErrorCode.PENDING_GUARDIAN_CONSENT);
        }
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new CustomException(AuthErrorCode.LOGIN_FAILED);
        }

        long refreshTokenTtl = Boolean.TRUE.equals(request.getRememberMe())
                ? jwtProperties.getRefreshTokenExpiration()
                : jwtProperties.getRefreshTokenShortExpiration();

        String accessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId(), refreshTokenTtl);
        Long memberId = member.getId();
        eventPublisher.publishEvent(new AfterCommitTask(this,
                () -> refreshTokenService.save(memberId, refreshToken, refreshTokenTtl)));

        return LoginResponseDto.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImageUrl())
                .role(member.getRole().name())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * 로그아웃. 클라이언트 토큰 폐기는 프론트 책임이며, 서버는 Redis의 Refresh Token만 즉시 삭제한다.
     */
    public void logout(Long memberId) {
        refreshTokenService.delete(memberId);
    }

    /**
     * Refresh Token 검증 후 Access Token만 재발급한다(Refresh Token은 갱신하지 않음).
     */
    public TokenRefreshResponseDto refreshAccessToken(TokenRefreshRequestDto request) {
        DecodedJWT decoded;
        try {
            decoded = jwtTokenProvider.verifyRefreshToken(request.getRefreshToken());
        } catch (ActionTokenExpiredException e) {
            throw new CustomException(AuthErrorCode.EXPIRED_REFRESH_TOKEN);
        } catch (ActionTokenInvalidException e) {
            throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long memberId = Long.valueOf(decoded.getSubject());
        if (!refreshTokenService.matches(memberId, request.getRefreshToken())) {
            throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        String newAccessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getRole().name());
        return TokenRefreshResponseDto.builder().accessToken(newAccessToken).build();
    }

    /**
     * 비밀번호 재설정 메일 발송. 소셜 계정은 비밀번호가 무작위 값으로 채워져 있어(OAuthService.signup 참고)
     * 여기서 재설정을 허용하면 이메일/비밀번호 로그인까지 뚫려버리므로 LOCAL 계정만 허용한다.
     */
    public void requestPasswordReset(PasswordResetRequestDto request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        if (member.getAuthProvider() != AuthProvider.LOCAL) {
            throw new CustomException(AuthErrorCode.OAUTH_ACCOUNT_PASSWORD_RESET_NOT_ALLOWED);
        }

        String passwordFingerprint = String.valueOf(member.getPassword().hashCode());
        String token = actionTokenProvider.create(
                PASSWORD_RESET_TOKEN_PURPOSE,
                member.getId(),
                PASSWORD_RESET_TOKEN_TTL_MILLIS,
                Map.of("pwv", passwordFingerprint)
        );

        mailService.sendPasswordResetEmail(member.getEmail(), member.getNickname(), token);
    }

    /**
     * 비밀번호 재설정 토큰 사전 검증(소비하지 않음). FE가 "토큰 입력" 단계에서 새 비밀번호를 받기 전에
     * 미리 유효성을 확인할 수 있도록 별도로 둔다. 실제 소비(consume)는 completePasswordReset에서만
     * 일어나므로, 이 메서드를 여러 번 호출해도 토큰이 무효화되지 않는다.
     */
    public void verifyPasswordResetToken(String token) {
        decodeAndValidatePasswordResetToken(token);
    }

    /**
     * 비밀번호 재설정 완료. 토큰 발급 시점의 비밀번호 지문(pwv)을 함께 검증해
     * 같은 토큰이 재사용(replay)되는 것을 막는다 (DB에 사용 여부를 별도로 저장하지 않음).
     */
    public void completePasswordReset(PasswordResetCompleteDto request) {
        validatePassword(request.getNewPassword());

        DecodedJWT decoded = decodeAndValidatePasswordResetToken(request.getToken());
        Long memberId = actionTokenProvider.getSubjectId(decoded);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        try {
            actionTokenProvider.consume(decoded);
        } catch (ActionTokenInvalidException e) {
            throw new CustomException(AuthErrorCode.INVALID_RESET_TOKEN);
        }

        member.changePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    /** 서명/만료(purpose 포함)와 발급 시점 비밀번호 지문(pwv)까지 검증한다. 토큰을 소비하지는 않는다. */
    private DecodedJWT decodeAndValidatePasswordResetToken(String token) {
        DecodedJWT decoded;
        try {
            decoded = actionTokenProvider.verify(token, PASSWORD_RESET_TOKEN_PURPOSE);
        } catch (ActionTokenExpiredException e) {
            throw new CustomException(AuthErrorCode.EXPIRED_RESET_TOKEN);
        } catch (ActionTokenInvalidException e) {
            throw new CustomException(AuthErrorCode.INVALID_RESET_TOKEN);
        }

        Long memberId = actionTokenProvider.getSubjectId(decoded);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        String currentFingerprint = String.valueOf(member.getPassword().hashCode());
        String tokenFingerprint = decoded.getClaim("pwv").asString();
        if (tokenFingerprint == null || !tokenFingerprint.equals(currentFingerprint)) {
            throw new CustomException(AuthErrorCode.INVALID_RESET_TOKEN);
        }

        return decoded;
    }
}
