package com.kosta.sangsangseoga.domain.friendLibrary.repository;
 
import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.friendLibrary.entity.AuthorFollow;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.Optional;
 
public interface AuthorFollowRepository extends JpaRepository<AuthorFollow, Long> {
 
    boolean existsByFollowerAndAuthor(Member follower, Member author);
 
    Optional<AuthorFollow> findByFollowerAndAuthor(Member follower, Member author);
 
    long countByAuthor(Member author);

    // 회원 탈퇴 시 해당 회원이 팔로우한 작가 목록 전체 삭제
    void deleteAllByFollower(Member follower);
}