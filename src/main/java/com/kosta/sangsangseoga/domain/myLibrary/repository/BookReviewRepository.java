package com.kosta.sangsangseoga.domain.myLibrary.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kosta.sangsangseoga.domain.myLibrary.entity.BookReview;

public interface BookReviewRepository extends JpaRepository<BookReview, Long> {
	
	//회원이 작성한 특정 책의 독후감 조회
	Optional<BookReview> findByMember_IdAndBook_Id(Long memberId, Long BookId);
	
	//회원이 작성한 모든 독후감 조회
	List<BookReview> findByMember_Id(Long memberId);
}
