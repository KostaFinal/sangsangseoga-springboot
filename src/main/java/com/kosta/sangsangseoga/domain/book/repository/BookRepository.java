package com.kosta.sangsangseoga.domain.book.repository;

import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.book.enums.BookStatus;
import com.kosta.sangsangseoga.domain.book.enums.BookType;
import org.springframework.data.domain.Page;
import com.kosta.sangsangseoga.domain.member.entity.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

	// 회원 탈퇴 시 처리 대상인 본인 소유 책 목록 조회
	List<Book> findAllByMember(Member member);

	// bookType/키워드 필터 + 페이징
	@Query("SELECT b FROM Book b WHERE b.status = 'PUBLISHED'" + " AND (:bookType IS NULL OR b.bookType = :bookType)"
			+ " AND (:keyword IS NULL OR b.title LIKE %:keyword% OR b.member.nickname LIKE %:keyword%)")
	Page<Book> findBooks(@Param("bookType") BookType bookType, @Param("keyword") String keyword, Pageable pageable);

	// popular 정렬 (조회수×1 + 좋아요×3)
	@Query("SELECT b FROM Book b WHERE b.status = 'PUBLISHED'" + " AND (:bookType IS NULL OR b.bookType = :bookType)"
			+ " AND (:keyword IS NULL OR b.title LIKE %:keyword% OR b.member.nickname LIKE %:keyword%)"
			+ " ORDER BY (b.viewCount * 1 + b.likeCount * 3) DESC")
	Page<Book> findBooksByPopular(@Param("bookType") BookType bookType, @Param("keyword") String keyword,
			Pageable pageable);

	// 같은 bookType에서 해당 책 제외, 좋아요 순 상위 N개 (추천용)
	@Query("SELECT b FROM Book b WHERE b.status = 'PUBLISHED' AND b.bookType = :bookType AND b.id != :excludeId ORDER BY b.likeCount DESC")
	List<Book> findRecommendations(@Param("bookType") BookType bookType, @Param("excludeId") Long excludeId,
			Pageable pageable);

	// 이번 주 신작 TOP5 조회
	@Query("SELECT b FROM Book b WHERE b.createdAt >= :weekStart AND b.status = 'PUBLISHED' "
			+ "ORDER BY (b.viewCount * 1 + b.likeCount * 3) DESC, b.createdAt DESC")
	List<Book> findTop5NewReleases(@Param("weekStart") LocalDateTime weekStart, Pageable pageable);

	// 전체 PUBLISHED 책 중 score 기준 상위 5개 (주간 랭킹 집계용)
	@Query("SELECT b FROM Book b WHERE b.status = 'PUBLISHED' " + "ORDER BY (b.viewCount * 1 + b.likeCount * 3) DESC")
	List<Book> findTop5ForWeeklyRanking(Pageable pageable);

	// 내가 작성한 공개 책 목록 조회
	List<Book> findByMember_IdAndStatus(Long memberId, BookStatus status);

    // 작가(회원)의 공개된 작품 수 (작가 검색/프로필용)
    long countByMemberAndStatus(Member member, BookStatus status);

    // 작가(회원)의 대표작품 - 좋아요 가장 많은 공개 작품 1건 (작가 검색/프로필용)
    Optional<Book> findTopByMemberAndStatusOrderByLikeCountDesc(Member member, BookStatus status);

}