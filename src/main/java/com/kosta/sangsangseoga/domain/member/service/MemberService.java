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
import com.kosta.sangsangseoga.domain.member.enums.GuardianConsentStatus;
import com.kosta.sangsangseoga.domain.member.enums.MemberStatus;
import com.kosta.sangsangseoga.domain.member.exception.MemberErrorCode;
import com.kosta.sangsangseoga.domain.member.repository.GuardianConsentRepository;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
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

        GuardianConsent consent = GuardianConsent.builder()
                .member(member)
                .guardianEmail(request.getGuardianEmail())
                .guardian(memberRepository.findByEmail(request.getGuardianEmail()).orElse(null))
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
            if (consent.getMember().getStatus() == MemberStatus.PENDING) {
                consent.getMember().activate();
            }
        } else {
            consent.reject();
        }
    }

    /**
     * 회원 탈퇴. 상태만 DELETED로 바꾸는 소프트 삭제이며(별도 파기 배치는 없음), 세션 무효화,
     * 구독 즉시 해지, 좋아요/북마크/관심작가 삭제, 작성 댓글 익명화, 내가 쓴 책 비공개 전환을 함께 한다.
     */
    public void withdraw(Long memberId, WithdrawRequestDto request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        if (member.getStatus() == MemberStatus.DELETED) {
            throw new CustomException(MemberErrorCode.ALREADY_DELETED_MEMBER);
        }
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new CustomException(MemberErrorCode.WRONG_PASSWORD);
        }

        member.cancelSubscriptionImmediately();
        member.withdraw();
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
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        member.updateViewerPreference(request.getViewerFontSize(), request.getViewerViewType());

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
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        String newNickname = request.getNickname();
        if (newNickname != null && !newNickname.equals(member.getNickname())
                && memberRepository.existsByNickname(newNickname)) {
            throw new CustomException(AuthErrorCode.DUPLICATE_NICKNAME);
        }

        member.updateProfile(newNickname, request.getProfileImageUrl(), request.getIntroduction());

        // 중복확인-저장 사이 동시 요청 대비: DB 유니크 제약 위반도 DUPLICATE_NICKNAME으로 변환한다.
        // 커밋 시점까지 미루면 500으로 새므로 여기서 강제로 flush해서 예외를 잡는다.
        try {
            memberRepository.saveAndFlush(member);
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
