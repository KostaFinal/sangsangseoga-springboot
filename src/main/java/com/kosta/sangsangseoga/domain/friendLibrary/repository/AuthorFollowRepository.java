package com.kosta.sangsangseoga.domain.friendLibrary.repository;
 
import com.kosta.sangsangseoga.domain.account.entity.Member;
import com.kosta.sangsangseoga.domain.friendLibrary.entity.AuthorFollow;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.Optional;
 
public interface AuthorFollowRepository extends JpaRepository<AuthorFollow, Long> {
 
    boolean existsByFollowerAndAuthor(Member follower, Member author);
 
    Optional<AuthorFollow> findByFollowerAndAuthor(Member follower, Member author);
 
    long countByAuthor(Member author);
}