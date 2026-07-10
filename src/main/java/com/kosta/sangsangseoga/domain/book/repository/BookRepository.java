package com.kosta.sangsangseoga.domain.book.repository;

import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.book.enums.BookStatus;
import com.kosta.sangsangseoga.domain.book.enums.BookType;
import org.springframework.data.domain.Page;
import com.kosta.sangsangseoga.domain.member.entity.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

	// 전체 PUBLISHED 책 중 이번 주 조회수/좋아요 기준 score 상위 5개 (주간 랭킹 집계용)
	// 동점이면 전체 누적 인기도(조회수×1+좋아요×3) 순, 그마저 같으면 최신 등록순으로 우선순위 결정
	@Query("SELECT b FROM Book b WHERE b.status = 'PUBLISHED' "
			+ "ORDER BY (b.weekViewCount * 1 + b.weekLikeCount * 3) DESC, "
			+ "(b.viewCount * 1 + b.likeCount * 3) DESC, b.createdAt DESC")
	List<Book> findTop5ForWeeklyRanking(Pageable pageable);

	// 주간 랭킹 집계 후 이번 주 조회수/좋아요를 0으로 초기화 (TOP5 여부와 무관하게 전체 PUBLISHED 책 대상)
	@Modifying
	@Query("UPDATE Book b SET b.weekViewCount = 0, b.weekLikeCount = 0 WHERE b.status = 'PUBLISHED'")
	void resetWeeklyCounters();

	// 내가 작성한 공개 책 목록 조회
	List<Book> findByMember_IdAndStatus(Long memberId, BookStatus status);

	// 작가(회원)의 공개된 작품 수 (작가 검색/프로필용)
	long countByMemberAndStatus(Member member, BookStatus status);

	// 작가(회원)의 대표작품 - 좋아요 가장 많은 공개 작품 1건 (작가 검색/프로필용)
	Optional<Book> findTopByMemberAndStatusOrderByLikeCountDesc(Member member, BookStatus status);

	// 여러 작가의 공개 작품 수를 작가별로 집계
	@Query("SELECT b.member.id, COUNT(b) "
	        + "FROM Book b "
	        + "WHERE b.member.id IN :authorIds "
	        + "AND b.status = :status "
	        + "GROUP BY b.member.id")
	List<Object[]> countPublishedBooksByAuthorIds(
	        @Param("authorIds") List<Long> authorIds,
	        @Param("status") BookStatus status
	);
	
	// 내가 작성한 모든 책 조회 - 공개/비공개 포함, 최신순
	List<Book> findByMember_IdOrderByCreatedAtDesc(Long memberId);
	

}