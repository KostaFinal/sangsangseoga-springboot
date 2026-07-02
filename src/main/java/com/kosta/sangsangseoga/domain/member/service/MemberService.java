package com.kosta.sangsangseoga.domain.member.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.book.repository.BookRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.repository.AuthorFollowRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.repository.BookmarkRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.repository.BookLikeRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.repository.CommentRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.entity.Comment;
import com.kosta.sangsangseoga.domain.myLibrary.repository.ReadingMemoRepository;
import com.kosta.sangsangseoga.domain.member.dto.GuardianConsentApproveRequestDto;
import com.kosta.sangsangseoga.domain.member.dto.GuardianConsentRequestDto;
import com.kosta.sangsangseoga.domain.member.dto.GuardianConsentResponseDto;
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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
    private final BookRepository bookRepository;
    private final BookLikeRepository bookLikeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final AuthorFollowRepository authorFollowRepository;
    private final CommentRepository commentRepository;
    private final MyReadingRepository myReadingRepository;
    private final ReadingMemoRepository readingMemoRepository;

    /**
     * 법정대리인 가입 동의 요청 생성. 실제 메일 발송(SMTP 등)은 별도 인프라 연동이 필요해
     * 이 메서드는 동의 이력 생성과 토큰 발급까지만 담당한다.
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

        // TODO: request.getGuardianEmail()로 token을 담은 동의 확인 메일 발송 (메일 인프라 연동 필요)

        return toResponseDto(consent);
    }

    /**
     * 법정대리인의 동의/거절 처리. 승인되면 대기 중인 미성년 회원을 활성화한다.
     */
    public GuardianConsentResponseDto processGuardianConsent(Long consentId, GuardianConsentApproveRequestDto request) {
        GuardianConsent consent = guardianConsentRepository.findById(consentId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.GUARDIAN_CONSENT_NOT_FOUND));

        if (consent.getStatus() != GuardianConsentStatus.REQUESTED) {
            throw new CustomException(MemberErrorCode.GUARDIAN_CONSENT_ALREADY_PROCESSED);
        }

        if (consent.getExpiresAt() != null && consent.getExpiresAt().isBefore(LocalDateTime.now())) {
            consent.expire();
            throw new CustomException(MemberErrorCode.GUARDIAN_CONSENT_EXPIRED);
        }

        if (request.getStatus() != GuardianConsentStatus.APPROVED && request.getStatus() != GuardianConsentStatus.REJECTED) {
            throw new CustomException(CommonErrorCode.BAD_REQUEST);
        }

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

        if (request.getStatus() == GuardianConsentStatus.APPROVED) {
            Member guardian = memberRepository.findByEmail(consent.getGuardianEmail()).orElse(null);
            consent.approve(guardian);
            if (consent.getMember().getStatus() == MemberStatus.PENDING) {
                consent.getMember().activate();
            }
        } else {
            consent.reject();
        }

        return toResponseDto(consent);
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
        refreshTokenService.delete(memberId);

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
            books.forEach(book -> book.setStatus("HIDDEN"));
            bookRepository.saveAll(books);
        }
    }

    private GuardianConsentResponseDto toResponseDto(GuardianConsent consent) {
        return GuardianConsentResponseDto.builder()
                .consentId(consent.getId())
                .status(consent.getStatus().name())
                .expiresAt(consent.getExpiresAt())
                .build();
    }
}
