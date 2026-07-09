package com.kosta.sangsangseoga.domain.friendLibrary.repository;
 
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kosta.sangsangseoga.domain.friendLibrary.entity.AuthorFollow;
import com.kosta.sangsangseoga.domain.member.entity.Member;
 
public interface AuthorFollowRepository extends JpaRepository<AuthorFollow, Long> {
 
    boolean existsByFollowerAndAuthor(Member follower, Member author);

    boolean existsByFollower_IdAndAuthor_Id(Long followerId, Long authorId);
 
    Optional<AuthorFollow> findByFollowerAndAuthor(Member follower, Member author);
 
    long countByAuthor(Member author);

    // 회원 탈퇴 시 해당 회원이 팔로우한 작가 목록 전체 삭제
    void deleteAllByFollower(Member follower);
    
    List<AuthorFollow> findByFollower_IdOrderByCreatedAtDesc(Long followerId);
}