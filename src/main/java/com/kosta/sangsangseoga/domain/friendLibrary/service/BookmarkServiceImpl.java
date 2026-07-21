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
     * 북마크 등록/이동
     * - 책당 북마크는 하나뿐 - 이미 다른 페이지에 북마크가 있으면 그 페이지로 옮김(pageNo 갱신)
     * - 이미 같은 페이지에 북마크되어 있으면 예외 발생(중복 등록)
     * - 등록/이동 후 bookId, pageNo, isBookmarkedByMe 반환
     */
    @Override
    public BookmarkDto addBookmark(Long memberId, Long bookId, Integer pageNo) throws Exception {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.BOOK_NOT_FOUND));

        Bookmark bookmark = bookmarkRepository.findByMemberAndBook(member, book).orElse(null);
        if (bookmark != null) {
            if (bookmark.getPageNo().equals(pageNo)) {
                throw new CustomException(FriendLibraryErrorCode.BOOKMARK_ALREADY_EXISTS);
            }
            bookmark.setPageNo(pageNo); // 영속 상태 - 트랜잭션 커밋 시 더티체킹으로 반영
        } else {
            bookmarkRepository.save(Bookmark.builder()
                    .member(member)
                    .book(book)
                    .pageNo(pageNo)
                    .build());
        }

        return BookmarkDto.builder()
                .bookId(bookId)
                .pageNo(pageNo)
                .isBookmarkedByMe(true)
                .build();
    }

    /**
     * 북마크 취소
     * - 책당 북마크가 하나뿐이라 페이지 구분 없이 그 책의 북마크를 삭제
     * - 북마크하지 않은 책인 경우 예외 발생
     * - DELETE 204 응답이라 반환값 없음
     */
    @Override
    public void removeBookmark(Long memberId, Long bookId) throws Exception {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.BOOK_NOT_FOUND));

        // 북마크 row 조회
        Bookmark bookmark = bookmarkRepository.findByMemberAndBook(member, book)
                .orElseThrow(() -> new CustomException(FriendLibraryErrorCode.BOOKMARK_NOT_FOUND));

        bookmarkRepository.delete(bookmark);
    }

    /**
     * 회원의 특정 책 북마크 조회 - 없으면 isBookmarkedByMe=false, pageNo=null
     */
    @Override
    @Transactional(readOnly = true)
    public BookmarkDto getBookmark(Long memberId, Long bookId) throws Exception {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.BOOK_NOT_FOUND));

        return bookmarkRepository.findByMemberAndBook(member, book)
                .map(b -> BookmarkDto.builder().bookId(bookId).pageNo(b.getPageNo()).isBookmarkedByMe(true).build())
                .orElse(BookmarkDto.builder().bookId(bookId).pageNo(null).isBookmarkedByMe(false).build());
    }
}