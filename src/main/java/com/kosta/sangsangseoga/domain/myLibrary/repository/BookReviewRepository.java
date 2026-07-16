package com.kosta.sangsangseoga.domain.myLibrary.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kosta.sangsangseoga.domain.myLibrary.entity.BookReview;

public interface BookReviewRepository extends JpaRepository<BookReview, Long> {

	// 회원이 특정 책에 작성한 독후감
	Optional<BookReview> findByMember_IdAndBook_Id(Long memberId, Long bookId);

	// 내 독후감 목록
	@EntityGraph(attributePaths = "book")
	List<BookReview> findByMember_IdOrderByCreatedAtDesc(Long memberId);

	// 내 독후감 상세 조회(수정/삭제 시 권한 확인까지 가능)
	Optional<BookReview> findByIdAndMember_Id(Long reviewId, Long memberId);

	// 회원이 작성한 독후감 개수
	Long countByMember_Id(Long memberId);
}
