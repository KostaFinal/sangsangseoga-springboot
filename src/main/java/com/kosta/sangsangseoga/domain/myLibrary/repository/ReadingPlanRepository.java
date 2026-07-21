package com.kosta.sangsangseoga.domain.myLibrary.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kosta.sangsangseoga.domain.myLibrary.entity.ReadingPlan;

public interface ReadingPlanRepository extends JpaRepository<ReadingPlan, Long> {
	// 독서 계획 목록 조회
    List<ReadingPlan> findByMember_IdOrderByPlanDateAsc(Long memberId);

    // 특정 날짜 계획 조회
    List<ReadingPlan> findByMember_IdAndPlanDateOrderByIdAsc(Long memberId, LocalDate planDate);

    // 수정/삭제/완료 처리용
    Optional<ReadingPlan> findByIdAndMember_Id(Long planId, Long memberId);

    // 같은 날짜에 같은 책 계획 중복 방지
    Optional<ReadingPlan> findByMember_IdAndBook_IdAndPlanDate(
            Long memberId,
            Long bookId,
            LocalDate planDate
    );
}
