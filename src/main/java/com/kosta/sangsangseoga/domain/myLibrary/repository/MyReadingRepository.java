package com.kosta.sangsangseoga.domain.myLibrary.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

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
	
	// 진행 중 책 조회
    Optional<MyReading> findByMember_IdAndBook_Id(
            Long memberId,
            Long bookId
    );

    // 읽는 중 목록
    List<MyReading> findByMember_IdAndReadingStatusOrderByRecentReadAtDesc(
            Long memberId,
            ReadingStatus readingStatus
    );

    // 읽기 완료 목록
    List<MyReading> findByMember_IdAndReadingStatusOrderByCompletedAtDesc(
            Long memberId,
            ReadingStatus readingStatus
    );
}
