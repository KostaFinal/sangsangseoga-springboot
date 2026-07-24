package com.kosta.sangsangseoga.domain.member.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.book.enums.BookStatus;
import com.kosta.sangsangseoga.domain.book.repository.BookRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.entity.Comment;
import com.kosta.sangsangseoga.domain.friendLibrary.repository.AuthorFollowRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.repository.BookLikeRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.repository.BookmarkRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.repository.CommentRepository;
import com.kosta.sangsangseoga.domain.member.dto.GuardianConsentApproveRequestDto;
import com.kosta.sangsangseoga.domain.member.dto.GuardianConsentDecisionRequestDto;
import com.kosta.sangsangseoga.domain.member.dto.GuardianConsentPendingResponseDto;
import com.kosta.sangsangseoga.domain.member.dto.GuardianConsentRequestDto;
import com.kosta.sangsangseoga.domain.member.dto.GuardianConsentResponseDto;
import com.kosta.sangsangseoga.domain.auth.exception.AuthErrorCode;
import com.kosta.sangsangseoga.domain.member.dto.MemberMeResponseDto;
import com.kosta.sangsangseoga.domain.member.dto.MemberUpdateRequestDto;
import com.kosta.sangsangseoga.domain.member.dto.NicknameCheckResponseDto;
import com.kosta.sangsangseoga.domain.member.dto.ProfileImageUploadResponseDto;
import com.kosta.sangsangseoga.domain.member.dto.ViewerPreferenceDto;
import com.kosta.sangsangseoga.domain.member.dto.WithdrawRequestDto;
import com.kosta.sangsangseoga.domain.member.entity.GuardianConsent;
import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.enums.AuthProvider;
import com.kosta.sangsangseoga.domain.member.enums.GuardianConsentStatus;
import com.kosta.sangsangseoga.domain.member.enums.MemberStatus;
import com.kosta.sangsangseoga.domain.member.exception.MemberErrorCode;
import com.kosta.sangsangseoga.domain.member.repository.GuardianConsentRepository;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
import com.kosta.sangsangseoga.domain.notification.service.NotificationService;
import com.kosta.sangsangseoga.domain.myLibrary.repository.MyReadingRepository;
import com.kosta.sangsangseoga.domain.myLibrary.repository.ReadingMemoRepository;
import com.kosta.sangsangseoga.global.event.AfterCommitTask;
import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;
import com.kosta.sangsangseoga.global.jwt.ActionTokenExpiredException;
import com.kosta.sangsangseoga.global.jwt.ActionTokenInvalidException;
import com.kosta.sangsangseoga.global.jwt.ActionTokenProvider;
import com.kosta.sangsangseoga.global.jwt.RefreshTokenService;

import com.kosta.sangsangseoga.global.mail.MailService;
import com.kosta.sangsangseoga.global.infra.storage.FileStorageService;


import com.kosta.sangsangseoga.global.jwt.TokenBlacklistService;


import java.time.Instant;



import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private static final String GUARDIAN_CONSENT_TOKEN_PURPOSE = "GUARDIAN_CONSENT";
    private static final long GUARDIAN_CONSENT_TOKEN_TTL_MILLIS = 7L * 24 * 60 * 60 * 1000; // 7일

    private final MemberRepository memberRepository;
    private final MemberOptimisticRetrySupport memberOptimisticRetrySupport;
    private final GuardianConsentRepository guardianConsentRepository;
    private final ActionTokenProvider actionTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final ApplicationEventPublisher eventPublisher;
    private final BookRepository bookRepository;
    private final BookLikeRepository bookLikeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final AuthorFollowRepository authorFollowRepository;
    private final CommentRepository commentRepository;
    private final MyReadingRepository myReadingRepository;
    private final ReadingMemoRepository readingMemoRepository;
    private final MailService mailService;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

    /**
     * 법정대리인 가입 동의 요청 생성.
     */
    public GuardianConsentResponseDto requestGuardianConsent(GuardianConsentRequestDto request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        if (guardianConsentRepository.existsByMemberIdAndStatus(member.getId(), GuardianConsentStatus.APPROVED)) {
            throw new CustomException(MemberErrorCode.ALREADY_APPROVED_MEMBER);
        }

        LocalDateTime requestedAt = LocalDateTime.now();
        LocalDateTime expiresAt = requestedAt.plusDays(7);

        Member guardian = memberRepository.findByEmail(request.getGuardianEmail()).orElse(null);

        GuardianConsent consent = GuardianConsent.builder()
                .member(member)
                .guardianEmail(request.getGuardianEmail())
                .guardian(guardian)
                .status(GuardianConsentStatus.REQUESTED)
                .requestedAt(requestedAt)
                .expiresAt(expiresAt)
                .build();
        guardianConsentRepository.save(consent);

        String token = actionTokenProvider.create(
                GUARDIAN_CONSENT_TOKEN_PURPOSE,
                consent.getId(),
                GUARDIAN_CONSENT_TOKEN_TTL_MILLIS
        );

        // 이메일 발송보다 먼저 실행한다. notify()가 예외를 던져 트랜잭션이 롤백되면 동의 요청 자체가
        // 없던 일이 되는데, 이메일을 먼저 보내버리면 보호자가 존재하지 않는 요청을 가리키는 링크를
        // 받게 된다. 이메일은 되돌릴 수 없는 외부 발송이라 반드시 트랜잭션 성공이 보장된 뒤에 보낸다.
        if (guardian != null) {
            notificationService.notify(guardian,
                    String.format("%s님이 보호자 동의를 요청했습니다.", member.getNickname()));
        }

        mailService.sendGuardianConsentEmail(request.getGuardianEmail(), member.getNickname(), consent.getId(), token);

        return toResponseDto(consent);
    }

    /**
     * 로그인한 보호자 계정 기준으로 자신에게 온 미처리(REQUESTED) 동의 요청 목록을 조회한다.
     * guardian_consent.guardian_id는 요청 생성 시점에 이메일로 이미 가입된 계정이 있어야 채워지므로,
     * 이후 가입한 보호자도 조회되도록 guardianEmail로 매칭한다.
     */
    @Transactional(readOnly = true)
    public List<GuardianConsentPendingResponseDto> getPendingGuardianConsents(Long guardianMemberId) {
        Member guardian = memberRepository.findById(guardianMemberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        return guardianConsentRepository
                .findByGuardianEmailAndStatusOrderByRequestedAtDesc(guardian.getEmail(), GuardianConsentStatus.REQUESTED)
                .stream()
                .map(this::toPendingResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * 법정대리인의 동의/거절 처리(이메일 링크, 비로그인). 승인되면 대기 중인 미성년 회원을 활성화한다.
     */
    public GuardianConsentResponseDto processGuardianConsent(Long consentId, GuardianConsentApproveRequestDto request) {
        GuardianConsent consent = loadPendingConsent(consentId);
        GuardianConsentStatus status = validateDecisionStatus(request.getStatus());

        DecodedJWT decoded;
        try {
            decoded = actionTokenProvider.verify(request.getToken(), GUARDIAN_CONSENT_TOKEN_PURPOSE);
            if (!actionTokenProvider.getSubjectId(decoded).equals(consent.getId())) {
                throw new ActionTokenInvalidException();
            }
            actionTokenProvider.consume(decoded);
        } catch (ActionTokenExpiredException e) {
            consent.expire();
            throw new CustomException(MemberErrorCode.GUARDIAN_CONSENT_EXPIRED);
        } catch (ActionTokenInvalidException e) {
            throw new CustomException(MemberErrorCode.INVALID_CONSENT_TOKEN);
        }

        Member guardian = memberRepository.findByEmail(consent.getGuardianEmail()).orElse(null);
        applyDecision(consent, status, guardian);

        return toResponseDto(consent);
    }

    /**
     * 법정대리인의 동의/거절 처리(로그인 상태). 이메일 토큰 없이, 로그인한 계정의 이메일이
     * 동의 요청의 guardianEmail과 일치하는지만 확인하고 처리한다.
     */
    public GuardianConsentResponseDto processGuardianConsentByLoggedInGuardian(
            Long consentId, Long guardianMemberId, GuardianConsentDecisionRequestDto request) {
        Member guardian = memberRepository.findById(guardianMemberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        GuardianConsent consent = loadPendingConsent(consentId);
        if (!consent.getGuardianEmail().equalsIgnoreCase(guardian.getEmail())) {
            throw new CustomException(MemberErrorCode.NOT_CONSENT_GUARDIAN);
        }

        GuardianConsentStatus status = validateDecisionStatus(request.getStatus());
        applyDecision(consent, status, guardian);

        return toResponseDto(consent);
    }

    /**
     * 이미 승인한 보호자가 동의를 철회한다. 승인(APPROVED) 상태인 건만 철회할 수 있고,
     * 철회되면 회원이 ACTIVE 상태였을 경우 재동의 전까지 PENDING으로 되돌려 로그인을 다시 막는다.
     */
    public GuardianConsentResponseDto withdrawGuardianConsent(Long consentId, Long guardianMemberId) {
        Member guardian = memberRepository.findById(guardianMemberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        GuardianConsent consent = guardianConsentRepository.findById(consentId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.GUARDIAN_CONSENT_NOT_FOUND));

        if (!consent.getGuardianEmail().equalsIgnoreCase(guardian.getEmail())) {
            throw new CustomException(MemberErrorCode.NOT_CONSENT_GUARDIAN);
        }
        if (consent.getStatus() != GuardianConsentStatus.APPROVED) {
            throw new CustomException(MemberErrorCode.GUARDIAN_CONSENT_NOT_APPROVED);
        }

        consent.withdraw();
        if (consent.getMember().getStatus() == MemberStatus.ACTIVE) {
            Long memberId = consent.getMember().getId();
            consent.getMember().revertToPending();
            eventPublisher.publishEvent(new AfterCommitTask(this, () -> {
                tokenBlacklistService.invalidateTokensIssuedBefore(memberId, Instant.now());
                refreshTokenService.delete(memberId);
            }));
            notificationService.notify(consent.getMember(), "보호자가 동의를 철회하여 계정 이용이 다시 제한되었습니다.");
        }

        return toResponseDto(consent);
    }

    private GuardianConsent loadPendingConsent(Long consentId) {
        GuardianConsent consent = guardianConsentRepository.findById(consentId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.GUARDIAN_CONSENT_NOT_FOUND));

        if (consent.getStatus() != GuardianConsentStatus.REQUESTED) {
            throw new CustomException(MemberErrorCode.GUARDIAN_CONSENT_ALREADY_PROCESSED);
        }

        if (consent.getExpiresAt() != null && consent.getExpiresAt().isBefore(LocalDateTime.now())) {
            consent.expire();
            throw new CustomException(MemberErrorCode.GUARDIAN_CONSENT_EXPIRED);
        }
        return consent;
    }

    private GuardianConsentStatus validateDecisionStatus(GuardianConsentStatus status) {
        if (status != GuardianConsentStatus.APPROVED && status != GuardianConsentStatus.REJECTED) {
            throw new CustomException(CommonErrorCode.BAD_REQUEST);
        }
        return status;
    }

    private void applyDecision(GuardianConsent consent, GuardianConsentStatus status, Member guardian) {
        if (status == GuardianConsentStatus.APPROVED) {
            consent.approve(guardian);
            boolean activated = consent.getMember().getStatus() == MemberStatus.PENDING;
            if (activated) {
                consent.getMember().activate();
            }
            notificationService.notify(consent.getMember(), activated
                    ? "보호자 동의가 승인되어 계정이 정상적으로 이용 가능합니다."
                    : "보호자 동의가 승인되었습니다.");
        } else {
            consent.reject();
            // 보호자가 거절하면 최초 가입 게이트를 통과하지 못한 것이다. 그대로 두면 회원이 PENDING에
            // 영원히 갇혀 로그인도 재가입도 못 하게 되므로, 가입을 취소 처리(DELETED)하고 email/
            // nickname/oauthProviderId를 풀어줘 같은 정보로 재가입할 수 있게 한다.
            if (consent.getMember().getStatus() == MemberStatus.PENDING) {
                consent.getMember().cancelPendingSignup();
            }
            notificationService.notify(consent.getMember(), "보호자가 동의를 거절해 가입이 취소되었습니다.");
        }
    }

    /**
     * 회원 탈퇴. 상태만 DELETED로 바꾸는 소프트 삭제이며(별도 파기 배치는 없음), 세션 무효화,
     * 구독 즉시 해지, 좋아요/북마크/관심작가 삭제, 작성 댓글 익명화, 내가 쓴 책 비공개 전환을 함께 한다.
     * email/nickname/oauthProviderId는 풀어줘(mangle) 같은 이메일/소셜 계정으로 재가입하면 완전히
     * 새 계정으로 시작할 수 있다. 관리자 강제 탈퇴({@code AdminService#changeMemberStatus})는 제재
     * 우회를 막기 위해 이 처리를 하지 않는다.
     */
    public void withdraw(Long memberId, WithdrawRequestDto request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        if (member.getStatus() == MemberStatus.DELETED) {
            throw new CustomException(MemberErrorCode.ALREADY_DELETED_MEMBER);
        }
        // 소셜 계정은 가입 시 무작위 UUID를 비밀번호로 채워둬(OAuthService.signup 참고) 본인도 알 수
        // 없으므로, 이미 JWT로 본인 인증이 끝난 상태를 본인 확인으로 갈음하고 비밀번호 검증을 건너뛴다.
        if (member.getAuthProvider() == AuthProvider.LOCAL
                && !passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new CustomException(MemberErrorCode.WRONG_PASSWORD);
        }

        member.cancelSubscriptionImmediately();
        member.withdrawAndReleaseIdentifiers();
        eventPublisher.publishEvent(new AfterCommitTask(this, () -> {
            tokenBlacklistService.invalidateTokensIssuedBefore(memberId, Instant.now());
            refreshTokenService.delete(memberId);
        }));

        bookLikeRepository.deleteAllByMember(member);
        bookmarkRepository.deleteAllByMember(member);
        authorFollowRepository.deleteAllByFollower(member);

        List<Comment> writtenComments = commentRepository.findAllByMember(member);
        writtenComments.forEach(comment -> comment.setMember(null));
        commentRepository.saveAll(writtenComments);

        hideMyBooks(member);
    }

    private void hideMyBooks(Member member) {
        List<Book> books = bookRepository.findAllByMember(member);
        books.forEach(book -> book.setStatus(BookStatus.HIDDEN));
        bookRepository.saveAll(books);
    }

    /**
     * 뷰어 환경설정(글자 크기/페이지 전환 방식) 조회.
     */
    @Transactional(readOnly = true)
    public ViewerPreferenceDto getViewerPreference(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        return ViewerPreferenceDto.builder()
                .viewerFontSize(member.getViewerFontSize())
                .viewerViewType(member.getViewerViewType())
                .build();
    }

    /**
     * 뷰어 환경설정(글자 크기/페이지 전환 방식) 저장.
     */
    public ViewerPreferenceDto updateViewerPreference(Long memberId, ViewerPreferenceDto request) {
        Member member = memberOptimisticRetrySupport.saveWithRetry(memberId,
                m -> m.updateViewerPreference(request.getViewerFontSize(), request.getViewerViewType()));

        return ViewerPreferenceDto.builder()
                .viewerFontSize(member.getViewerFontSize())
                .viewerViewType(member.getViewerViewType())
                .build();
    }

    private GuardianConsentResponseDto toResponseDto(GuardianConsent consent) {
        return GuardianConsentResponseDto.builder()
                .consentId(consent.getId())
                .status(consent.getStatus().name())
                .expiresAt(consent.getExpiresAt())
                .build();
    }

    private GuardianConsentPendingResponseDto toPendingResponseDto(GuardianConsent consent) {
        Member member = consent.getMember();
        return GuardianConsentPendingResponseDto.builder()
                .consentId(consent.getId())
                .memberId(member.getId())
                .memberNickname(member.getNickname())
                .memberEmail(member.getEmail())
                .memberBirthDate(member.getBirthDate())
                .requestedAt(consent.getRequestedAt())
                .expiresAt(consent.getExpiresAt())
                .build();
    }
    
    @Transactional(readOnly = true)
    public MemberMeResponseDto getMyInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        return MemberMeResponseDto.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImageUrl())
                .introduction(member.getIntroduction())
                .build();
    }

    /**
     * 회원정보(닉네임/프로필 이미지/소개) 수정. 요청에서 null인 필드는 그대로 유지한다.
     * 닉네임을 바꾸는 경우에만, 그리고 기존 닉네임과 실제로 다를 때만 중복 검사를 한다.
     */
    public MemberMeResponseDto updateMyInfo(Long memberId, MemberUpdateRequestDto request) {
        String newNickname = request.getNickname();

        // 닉네임 중복 검사는 saveWithRetry가 재시도할 때마다(최신 Member 기준으로) 다시 수행한다.
        // 한 번만 검사하면, 재시도 사이에 다른 요청이 그 닉네임을 선점해도 놓치게 된다.
        // 유니크 제약 위반(DataIntegrityViolationException)은 동시 요청이 검사 통과 직후 같은
        // 닉네임으로 먼저 커밋한 경우로, DUPLICATE_NICKNAME으로 변환한다.
        Member member;
        try {
            member = memberOptimisticRetrySupport.saveWithRetry(memberId, m -> {
                if (newNickname != null && !newNickname.equals(m.getNickname())
                        && memberRepository.existsByNickname(newNickname)) {
                    throw new CustomException(AuthErrorCode.DUPLICATE_NICKNAME);
                }
                m.updateProfile(newNickname, request.getProfileImageUrl(), request.getIntroduction());
            });
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(AuthErrorCode.DUPLICATE_NICKNAME);
        }

        return MemberMeResponseDto.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImageUrl())
                .introduction(member.getIntroduction())
                .build();
    }

    /**
     * 닉네임 사용 가능 여부 확인. 비로그인 호출도 허용한다(memberId=null).
     * 로그인 상태에서 자기 자신이 이미 쓰고 있는 닉네임을 그대로 검사하면 available=true를 돌려준다.
     */
    @Transactional(readOnly = true)
    public NicknameCheckResponseDto checkNicknameAvailable(String nickname, Long memberId) {
        boolean isOwnNickname = false;
        if (memberId != null) {
            Member member = memberRepository.findById(memberId).orElse(null);
            isOwnNickname = member != null && nickname.equals(member.getNickname());
        }

        boolean available = isOwnNickname || !memberRepository.existsByNickname(nickname);
        return NicknameCheckResponseDto.builder()
                .available(available)
                .build();
    }

    /**
     * 프로필 사진 업로드. 회원 엔티티(profileImageUrl)에는 저장하지 않고 업로드된 URL만 돌려준다.
     * 실제 프로필 반영은 클라이언트가 이 URL로 별도 회원정보 수정 API를 호출해서 저장하는 흐름이다.
     */
    public ProfileImageUploadResponseDto uploadProfileImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(MemberErrorCode.EMPTY_FILE);
        }
        if (!ALLOWED_IMAGE_CONTENT_TYPES.contains(file.getContentType())) {
            throw new CustomException(MemberErrorCode.INVALID_IMAGE_FILE);
        }

        String profileImageUrl = fileStorageService.store(file, "profile-images");
        return ProfileImageUploadResponseDto.builder()
                .profileImageUrl(profileImageUrl)
                .build();
    }
}
