package com.kosta.sangsangseoga.domain.auth.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.kosta.sangsangseoga.domain.auth.dto.LoginRequestDto;
import com.kosta.sangsangseoga.domain.auth.dto.LoginResponseDto;
import com.kosta.sangsangseoga.domain.auth.dto.PasswordResetCompleteDto;
import com.kosta.sangsangseoga.domain.auth.dto.PasswordResetRequestDto;
import com.kosta.sangsangseoga.domain.auth.dto.TokenRefreshRequestDto;
import com.kosta.sangsangseoga.domain.auth.dto.TokenRefreshResponseDto;
import com.kosta.sangsangseoga.domain.auth.exception.AuthErrorCode;
import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.enums.MemberStatus;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;
import com.kosta.sangsangseoga.global.jwt.ActionTokenExpiredException;
import com.kosta.sangsangseoga.global.jwt.ActionTokenInvalidException;
import com.kosta.sangsangseoga.global.jwt.ActionTokenProvider;
import com.kosta.sangsangseoga.global.jwt.JwtTokenProvider;
import com.kosta.sangsangseoga.global.jwt.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private static final String PASSWORD_RESET_TOKEN_PURPOSE = "PASSWORD_RESET";
    private static final long PASSWORD_RESET_TOKEN_TTL_MILLIS = 30 * 60 * 1000L; // 30분
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActionTokenProvider actionTokenProvider;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    /**
     * 이메일/비밀번호 로그인. Access/Refresh Token을 동시 발급하고,
     * Refresh Token은 회원 ID를 키로 Redis 화이트리스트에 저장한다.
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
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new CustomException(AuthErrorCode.LOGIN_FAILED);
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());
        refreshTokenService.save(member.getId(), refreshToken);

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
     * 비밀번호 재설정 인증 메일 발송 요청.
     * 실제 메일 발송(SMTP 등)은 별도 인프라 연동이 필요해 이 메서드는 토큰 발급까지만 담당한다.
     */
    public void requestPasswordReset(PasswordResetRequestDto request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        String passwordFingerprint = String.valueOf(member.getPassword().hashCode());
        String token = actionTokenProvider.create(
                PASSWORD_RESET_TOKEN_PURPOSE,
                member.getId(),
                PASSWORD_RESET_TOKEN_TTL_MILLIS,
                Map.of("pwv", passwordFingerprint)
        );

        // TODO: member.getEmail()로 token을 담은 비밀번호 재설정 링크 메일 발송 (메일 인프라 연동 필요)
    }

    /**
     * 비밀번호 재설정 완료. 토큰 발급 시점의 비밀번호 지문(pwv)을 함께 검증해
     * 같은 토큰이 재사용(replay)되는 것을 막는다 (DB에 사용 여부를 별도로 저장하지 않음).
     */
    public void completePasswordReset(PasswordResetCompleteDto request) {
        if (request.getNewPassword() == null || request.getNewPassword().length() < MIN_PASSWORD_LENGTH) {
            throw new CustomException(AuthErrorCode.WEAK_PASSWORD);
        }

        DecodedJWT decoded;
        try {
            decoded = actionTokenProvider.verify(request.getToken(), PASSWORD_RESET_TOKEN_PURPOSE);
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
        actionTokenProvider.consume(decoded);

        member.changePassword(passwordEncoder.encode(request.getNewPassword()));
    }
}
