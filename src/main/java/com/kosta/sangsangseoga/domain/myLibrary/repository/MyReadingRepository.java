package com.kosta.sangsangseoga.domain.myLibrary.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.myLibrary.entity.MyReading;
import com.kosta.sangsangseoga.domain.myLibrary.enums.ReadingStatus;
public interface MyReadingRepository extends JpaRepository<MyReading, Long> {
	
	//읽고 싶은 책 목록 조회
	List<MyReading> findByMember_IdAndReadingStatus(
			Long memberId,
			ReadingStatus readingStatus
	);
	
	// 내가 찜한 책이 맞는지 확인
	Optional<MyReading> findByMember_IdAndBook_IdAndReadingStatus(
			Long memberId,
			Long bookId,
			ReadingStatus readingStatus
	);

	// 책 삭제 시 해당 책의 독서 기록 전체 삭제
	void deleteAllByBook_Id(Long bookId);
	
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
}
