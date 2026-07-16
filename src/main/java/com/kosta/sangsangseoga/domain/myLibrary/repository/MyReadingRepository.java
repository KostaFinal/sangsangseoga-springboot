package com.kosta.sangsangseoga.domain.myLibrary.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.book.enums.BookStatus;
import com.kosta.sangsangseoga.domain.myLibrary.entity.MyReading;
import com.kosta.sangsangseoga.domain.myLibrary.enums.ReadingStatus;

public interface MyReadingRepository extends JpaRepository<MyReading, Long> {
	
	// 읽고 싶은 책 목록 조회
	@EntityGraph(attributePaths = "book")
	List<MyReading> findByMember_IdAndWishlistTrue(Long memberId);

	// 내가 읽고 싶은 책으로 등록한 책인지 확인
	Optional<MyReading> findByMember_IdAndBook_IdAndWishlistTrue(Long memberId, Long bookId);

	// 읽고 싶은 책 개수
	Long countByMember_IdAndWishlistTrue(Long memberId);

	// 읽고 싶은 책 목록 조회
	List<MyReading> findByMember_IdAndReadingStatus(Long memberId, ReadingStatus readingStatus);

	// 내가 찜한 책이 맞는지 확인
	Optional<MyReading> findByMember_IdAndBook_IdAndReadingStatus(Long memberId, Long bookId,
			ReadingStatus readingStatus);

	// 책 삭제 시 해당 책의 독서 기록 전체 삭제
	void deleteAllByBook_Id(Long bookId);

	// 진행 중 책 조회
	Optional<MyReading> findByMember_IdAndBook_Id(Long memberId, Long bookId);

	// 읽는 중 목록
	List<MyReading> findByMember_IdAndReadingStatusOrderByRecentReadAtDesc(Long memberId, ReadingStatus readingStatus);

	// 읽기 완료 목록
	List<MyReading> findByMember_IdAndReadingStatusOrderByCompletedAtDesc(Long memberId, ReadingStatus readingStatus);

	// 상태별 책 개수
	Long countByMember_IdAndReadingStatus(Long memberId, ReadingStatus readingStatus);

	@Query("select coalesce(sum(m.readingTime), 0) from MyReading m where m.member.id = :memberId")
	Long sumReadingTimeByMemberId(@Param("memberId") Long memberId);
	
	// 회원의 독서 통계 기록
	List<MyReading> findByMember_Id(Long memberId);
	
	// 협업 필터링: 이 책을 읽은 사람들이 함께 읽은 다른 책 중 가장 많이 겹치는 것
    // 해당 책 제외, READING 또는 COMPLETED 상태인 것만
    @Query("SELECT mr.book FROM MyReading mr " +
           "WHERE mr.member IN (" +
           "  SELECT mr2.member FROM MyReading mr2 WHERE mr2.book = :book" +
           "  AND mr2.readingStatus IN ('READING', 'COMPLETED')" +
           ") " +
           "AND mr.book != :book " +
           "AND mr.book.status = 'PUBLISHED' " +
           "AND mr.readingStatus IN ('READING', 'COMPLETED') " +
           "GROUP BY mr.book " +
           "ORDER BY COUNT(mr.member) DESC")
    List<Book> findCollaborativeRecommendations(@Param("book") Book book, Pageable pageable);
    
    List<MyReading> findByMember_IdAndRereadCountGreaterThanOrderByCompletedAtDesc(
            Long memberId,
            Integer rereadCount
    );
    
    // 개수만 반환
    Long countByMember_IdAndRereadCountGreaterThan(Long memberId, Integer rereadCount);
    
    // 목록 반환
    List<MyReading> findByMember_IdAndRereadCountGreaterThan(Long memberId, Integer rereadCount);
    
 // 삭제되지 않은 읽는 중 책 목록
    @EntityGraph(attributePaths = "book")
    Slice<MyReading> findByMember_IdAndReadingStatusAndBook_StatusNotOrderByRecentReadAtDesc(
            Long memberId,
            ReadingStatus readingStatus,
            BookStatus bookStatus,
            Pageable pageable
    );
}
