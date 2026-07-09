package com.kosta.sangsangseoga.domain.member.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.book.enums.BookStatus;
import com.kosta.sangsangseoga.domain.book.repository.BookRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.repository.AuthorFollowRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.repository.BookmarkRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.repository.BookLikeRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.repository.CommentRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.entity.Comment;
import com.kosta.sangsangseoga.domain.myLibrary.repository.ReadingMemoRepository;
import com.kosta.sangsangseoga.domain.member.dto.GuardianConsentApproveRequestDto;
import com.kosta.sangsangseoga.domain.member.dto.GuardianConsentDecisionRequestDto;
import com.kosta.sangsangseoga.domain.member.dto.GuardianConsentPendingResponseDto;
import com.kosta.sangsangseoga.domain.member.dto.GuardianConsentRequestDto;
import com.kosta.sangsangseoga.domain.member.dto.GuardianConsentResponseDto;
import com.kosta.sangsangseoga.domain.member.dto.ViewerPreferenceDto;
import com.kosta.sangsangseoga.domain.member.dto.WithdrawRequestDto;
import com.kosta.sangsangseoga.domain.member.entity.GuardianConsent;
import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.enums.GuardianConsentStatus;
import com.kosta.sangsangseoga.domain.member.enums.MemberStatus;
import com.kosta.sangsangseoga.domain.member.enums.WithdrawalBookPolicy;
import com.kosta.sangsangseoga.domain.member.exception.MemberErrorCode;
import com.kosta.sangsangseoga.domain.member.repository.GuardianConsentRepository;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
import com.kosta.sangsangseoga.domain.myLibrary.repository.MyReadingRepository;
import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;
import com.kosta.sangsangseoga.global.jwt.ActionTokenExpiredException;
import com.kosta.sangsangseoga.global.jwt.ActionTokenInvalidException;
import com.kosta.sangsangseoga.global.jwt.ActionTokenProvider;
import com.kosta.sangsangseoga.global.jwt.RefreshTokenService;
import com.kosta.sangsangseoga.global.event.AfterCommitTask;
import com.kosta.sangsangseoga.global.mail.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private final ApplicationEventPublisher eventPublisher;
    private final BookRepository bookRepository;
    private final BookLikeRepository bookLikeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final AuthorFollowRepository authorFollowRepository;
    private final CommentRepository commentRepository;
    private final MyReadingRepository myReadingRepository;
    private final ReadingMemoRepository readingMemoRepository;
    private final MailService mailService;

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
            consent.getMember().revertToPending();
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
     * 회원 탈퇴. 상태를 DELETED로 전환(하드 삭제 아님 - 개인정보 보관 정책은 별도 파기 배치가 담당),
     * Redis Refresh Token 삭제, 구독 즉시 해지, 좋아요/북마크/관심작가 삭제, 작성 댓글 익명화,
     * 공개 도서 처리(비공개 전환/삭제)를 수행한다.
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
        eventPublisher.publishEvent(new AfterCommitTask(this, () -> refreshTokenService.delete(memberId)));

        bookLikeRepository.deleteAllByMember(member);
        bookmarkRepository.deleteAllByMember(member);
        authorFollowRepository.deleteAllByFollower(member);

        List<Comment> writtenComments = commentRepository.findAllByMember(member);
        writtenComments.forEach(comment -> comment.setMember(null));
        commentRepository.saveAll(writtenComments);

        applyBookPolicy(member, request.getBookPolicy());
    }

    private void applyBookPolicy(Member member, WithdrawalBookPolicy bookPolicy) {
        List<Book> books = bookRepository.findAllByMember(member);

        if (bookPolicy == WithdrawalBookPolicy.DELETE) {
            // NOTE: book_page/book_image/book_tag/book_review 엔티티가 아직 필드 없는 스켈레톤이라
            // 이 테이블들의 book_id 참조 행을 먼저 지울 방법이 없다. 이 상태에서 실제 페이지·이미지
            // 데이터가 쌓인 책을 delete()하면 FK 제약으로 실패한다.
            // 위 엔티티들이 채워진 뒤 해당 리포지토리로 선삭제하는 로직을 추가해야 완전해진다.
            for (Book book : books) {
                commentRepository.deleteAllByBook(book);
                bookLikeRepository.deleteAllByBook(book);
                bookmarkRepository.deleteAllByBook(book);
                myReadingRepository.deleteAllByBook_Id(book.getId());
                readingMemoRepository.deleteAllByBook(book);
            }
            bookRepository.deleteAll(books);
        } else {
            books.forEach(book -> book.setStatus(BookStatus.HIDDEN));
            bookRepository.saveAll(books);
        }
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
}
