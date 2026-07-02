package com.kosta.sangsangseoga.domain.friendLibrary.service;
 
import com.kosta.sangsangseoga.domain.friendLibrary.dto.BookmarkDto;
 
public interface BookmarkService {
 
    // 북마크 등록 - 201 응답, bookId/pageNo/isBookmarkedByMe 반환
    BookmarkDto addBookmark(Long memberId, Long bookId, Integer pageNo) throws Exception;
 
    // 북마크 취소 - 204 응답, 반환값 없음
    void removeBookmark(Long memberId, Long bookId, Integer pageNo) throws Exception;
}