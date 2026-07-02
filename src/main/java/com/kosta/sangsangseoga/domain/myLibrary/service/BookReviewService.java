package com.kosta.sangsangseoga.domain.myLibrary.service;

import com.kosta.sangsangseoga.domain.myLibrary.dto.AiFeedbackResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.BookReviewRequestDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.BookReviewResponseDto;

public interface BookReviewService {
	
	//독후감 작성
	void createBookReview(Long memberId, Long bookId, BookReviewRequestDto bookReviewRequestDto) throws Exception;
	
	//독후감 조회
	BookReviewResponseDto getBookReview(Long memberId, Long bookId) throws Exception;
	
	//독후감 수정
	void updateBookReview(Long memberId, Long bookId, BookReviewRequestDto bookReviewRequestDto) throws Exception;
	
	//독후감 삭제
	void deleteBookReview(Long memberId, Long bookId) throws Exception;
	
	//AI 피드백 조회
	AiFeedbackResponseDto getAiFeedback(Long memberId, Long bookId) throws Exception;
}
