package com.kosta.sangsangseoga.domain.friendLibrary.service;
 
import com.kosta.sangsangseoga.domain.friendLibrary.dto.AuthorFollowDto;
import com.kosta.sangsangseoga.domain.friendLibrary.dto.AuthorListResponseDto;
 
public interface AuthorFollowService {
 
    // 팔로우 - 팔로우 후 결과 반환
    AuthorFollowDto follow(Long followerId, Long authorId) throws Exception;
 
    // 언팔로우 - 204 응답이라 반환값 없음
    void unfollow(Long followerId, Long authorId) throws Exception;
    
    AuthorListResponseDto getMyFollowedAuthors(Long memberId, int page, int size) throws Exception;
}