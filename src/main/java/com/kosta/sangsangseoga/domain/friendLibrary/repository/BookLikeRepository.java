package com.kosta.sangsangseoga.domain.friendLibrary.repository;

import com.kosta.sangsangseoga.domain.account.entity.Member;
import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.friendLibrary.entity.BookLike;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookLikeRepository extends JpaRepository<BookLike, Long> {
	
	 // 회원이 해당 책에 좋아요를 눌렀는지 여부 확인
    boolean existsByMemberAndBook(Member member, Book book);
 
    // 회원과 책으로 좋아요 row 조회 (좋아요 취소 시 사용)
    Optional<BookLike> findByMemberAndBook(Member member, Book book);
}
