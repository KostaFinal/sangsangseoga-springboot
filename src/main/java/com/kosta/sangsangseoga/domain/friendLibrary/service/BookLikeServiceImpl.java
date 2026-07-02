package com.kosta.sangsangseoga.domain.friendLibrary.service;

import com.kosta.sangsangseoga.domain.account.entity.Member;
import com.kosta.sangsangseoga.domain.account.repository.MemberRepository;
import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.book.repository.BookRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.dto.BookLikeDto;
import com.kosta.sangsangseoga.domain.friendLibrary.entity.BookLike;
import com.kosta.sangsangseoga.domain.friendLibrary.exception.FriendLibraryErrorCode;
import com.kosta.sangsangseoga.domain.friendLibrary.repository.BookLikeRepository;
import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BookLikeServiceImpl implements BookLikeService {

    private final BookLikeRepository bookLikeRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;

    /**
     * 좋아요 추가
     * - 이미 좋아요를 누른 경우 예외 발생
     * - 좋아요 추가 후 bookId, likeCount, isLikedByMe 반환
     */
    @Override
    public BookLikeDto like(Long memberId, Long bookId) throws Exception {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.BOOK_NOT_FOUND));

        // 중복 좋아요 방지
        if (bookLikeRepository.existsByMemberAndBook(member, book)) {
            throw new CustomException(FriendLibraryErrorCode.LIKE_ALREADY_EXISTS);
        }

        bookLikeRepository.save(BookLike.builder()
                .member(member)
                .book(book)
                .build());

        // 좋아요 수 증가 후 현재 값 반환
        book.setLikeCount(book.getLikeCount() + 1);

        return BookLikeDto.builder()
                .bookId(bookId)
                .likeCount(book.getLikeCount())
                .isLikedByMe(true)
                .build();
    }

    /**
     * 좋아요 취소
     * - 좋아요를 누르지 않은 경우 예외 발생
     * - DELETE 204 응답이라 반환값 없음
     */
    @Override
    public void unlike(Long memberId, Long bookId) throws Exception {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.BOOK_NOT_FOUND));

        // 좋아요 row 조회
        BookLike bookLike = bookLikeRepository.findByMemberAndBook(member, book)
                .orElseThrow(() -> new CustomException(FriendLibraryErrorCode.LIKE_NOT_FOUND));

        bookLikeRepository.delete(bookLike);

        // 좋아요 수 감소 (0 이하로 내려가지 않도록)
        book.setLikeCount(Math.max(0, book.getLikeCount() - 1));
    }
}