package com.kosta.sangsangseoga.domain.myLibrary.service;

import java.time.LocalDate;
import java.util.List;

import com.kosta.sangsangseoga.domain.myLibrary.dto.ReadingPlanRequestDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.ReadingPlanResponseDto;

public interface ReadingPlanService {
	// 독서 계획 목록 조회
    List<ReadingPlanResponseDto> getReadingPlans(Long memberId);

    // 특정 날짜 독서 계획 조회
    List<ReadingPlanResponseDto> getReadingPlansByDate(Long memberId, LocalDate planDate);

    // 독서 계획 등록
    ReadingPlanResponseDto createReadingPlan(Long memberId, ReadingPlanRequestDto requestDto);

    // 독서 계획 수정
    ReadingPlanResponseDto updateReadingPlan(Long memberId, Long planId,
                                             ReadingPlanRequestDto requestDto);

    // 독서 계획 삭제
    void deleteReadingPlan(Long memberId, Long planId);

    // 독서 계획 완료
    ReadingPlanResponseDto completeReadingPlan(Long memberId, Long planId);
}
