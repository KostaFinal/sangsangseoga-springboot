package com.kosta.sangsangseoga.domain.friendLibrary.service;
 
import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.book.repository.BookRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.dto.BookmarkDto;
import com.kosta.sangsangseoga.domain.friendLibrary.entity.Bookmark;
import com.kosta.sangsangseoga.domain.friendLibrary.exception.FriendLibraryErrorCode;
import com.kosta.sangsangseoga.domain.friendLibrary.repository.BookmarkRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.service.BookmarkService;
import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
@Service
@RequiredArgsConstructor
@Transactional
public class BookmarkServiceImpl implements BookmarkService {
 
    private final BookmarkRepository bookmarkRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
 
    /**
     * 북마크 등록
     * - 이미 북마크한 페이지인 경우 예외 발생
     * - 등록 후 bookId, pageNo, isBookmarkedByMe 반환
     */
    @Override
    public BookmarkDto addBookmark(Long memberId, Long bookId, Integer pageNo) throws Exception {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));
 
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.BOOK_NOT_FOUND));
 
        // 중복 북마크 방지
        if (bookmarkRepository.existsByMemberAndBookAndPageNo(member, book, pageNo)) {
            throw new CustomException(FriendLibraryErrorCode.BOOKMARK_ALREADY_EXISTS);
        }
 
        bookmarkRepository.save(Bookmark.builder()
                .member(member)
                .book(book)
                .pageNo(pageNo)
                .build());
 
        return BookmarkDto.builder()
                .bookId(bookId)
                .pageNo(pageNo)
                .isBookmarkedByMe(true)
                .build();
    }
 
    /**
     * 북마크 취소
     * - 북마크하지 않은 페이지인 경우 예외 발생
     * - DELETE 204 응답이라 반환값 없음
     */
    @Override
    public void removeBookmark(Long memberId, Long bookId, Integer pageNo) throws Exception {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));
 
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.BOOK_NOT_FOUND));
 
        // 북마크 row 조회
        Bookmark bookmark = bookmarkRepository.findByMemberAndBookAndPageNo(member, book, pageNo)
                .orElseThrow(() -> new CustomException(FriendLibraryErrorCode.BOOKMARK_NOT_FOUND));
 
        bookmarkRepository.delete(bookmark);
    }
}