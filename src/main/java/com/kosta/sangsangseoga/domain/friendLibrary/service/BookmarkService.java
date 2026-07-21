package com.kosta.sangsangseoga.domain.friendLibrary.service;

import com.kosta.sangsangseoga.domain.friendLibrary.dto.BookmarkDto;

public interface BookmarkService {

    // 북마크 등록/이동 - 책당 북마크가 하나뿐이라 이미 있으면 pageNo만 갱신 - 201 응답
    BookmarkDto addBookmark(Long memberId, Long bookId, Integer pageNo) throws Exception;

    // 북마크 취소 - 204 응답, 반환값 없음
    void removeBookmark(Long memberId, Long bookId) throws Exception;

    // 회원의 특정 책 북마크 조회 (없으면 isBookmarkedByMe=false, pageNo=null)
    BookmarkDto getBookmark(Long memberId, Long bookId) throws Exception;
}