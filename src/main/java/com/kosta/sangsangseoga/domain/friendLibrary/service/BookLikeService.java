package com.kosta.sangsangseoga.domain.friendLibrary.service;
 
import com.kosta.sangsangseoga.domain.friendLibrary.dto.BookLikeDto;
 
public interface BookLikeService {
 
    // 좋아요 추가 - 201 응답, bookId/likeCount/isLikedByMe 반환
    BookLikeDto like(Long memberId, Long bookId) throws Exception;
 
    // 좋아요 취소 - 204 응답, 반환값 없음
    void unlike(Long memberId, Long bookId) throws Exception;
}
 