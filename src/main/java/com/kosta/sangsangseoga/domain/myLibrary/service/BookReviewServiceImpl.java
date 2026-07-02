package com.kosta.sangsangseoga.domain.myLibrary.service;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.kosta.sangsangseoga.domain.myLibrary.dto.AiFeedbackResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.BookReviewRequestDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.BookReviewResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.repository.BookReviewRepository;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
@Transactional
public class BookReviewServiceImpl implements BookReviewService {

	private final BookReviewRepository bookReviewRepository;
	
	@Override
	public void createBookReview(Long memberId, Long bookId, BookReviewRequestDto bookReviewRequestDto)
			throws Exception {
		// member, book entity

	}

	@Override
	public BookReviewResponseDto getBookReview(Long memberId, Long bookId) throws Exception {
		return null;
	}

	@Override
	public void updateBookReview(Long memberId, Long bookId, BookReviewRequestDto bookReviewRequestDto)
			throws Exception {

	}

	@Override
	public void deleteBookReview(Long memberId, Long bookId) throws Exception {

	}

	@Override
	public AiFeedbackResponseDto getAiFeedback(Long memberId, Long bookId) throws Exception {
		return null;
	}

}
