package com.kosta.sangsangseoga.domain.myLibrary.service;

import java.util.List;

import com.kosta.sangsangseoga.domain.myLibrary.dto.AiFeedbackResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.BookReviewRequestDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.BookReviewResponseDto;

public interface BookReviewService {
	
	 // 독후감 목록 조회
    List<BookReviewResponseDto> getReviews(Long memberId) throws Exception;

    // 독후감 상세 조회
    BookReviewResponseDto getReview(Long memberId, Long reviewId) throws Exception;

    // 독후감 작성
    BookReviewResponseDto createReview(Long memberId, BookReviewRequestDto requestDto) throws Exception;

    // 독후감 수정
    BookReviewResponseDto updateReview(Long memberId, Long reviewId, BookReviewRequestDto requestDto) throws Exception;

    // 독후감 삭제
    void deleteReview(Long memberId, Long reviewId) throws Exception;

    // 독후감 임시저장
    BookReviewResponseDto saveDraft(Long memberId, Long reviewId, BookReviewRequestDto requestDto) throws Exception;

    // AI 피드백 조회
    AiFeedbackResponseDto getAiFeedback(Long memberId, Long reviewId) throws Exception;
}
