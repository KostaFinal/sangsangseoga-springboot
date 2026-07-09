package com.kosta.sangsangseoga.domain.myLibrary.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kosta.sangsangseoga.domain.myLibrary.dto.AiFeedbackResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.BookReviewRequestDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.BookReviewResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.service.BookReviewService;
import com.kosta.sangsangseoga.global.common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bookshelves/reviews")
@RequiredArgsConstructor
public class BookReviewController {
	private final BookReviewService bookReviewService;

	// 독후감 목록 조회
	@GetMapping
	public ResponseEntity<ApiResponse<List<BookReviewResponseDto>>> getReviews(Authentication authentication) {
	    Long memberId = (Long) authentication.getPrincipal();
	    return ResponseEntity.ok(ApiResponse.success(bookReviewService.getReviews(memberId)));
	}

	// 독후감 상세 조회
	@GetMapping("/{reviewId}")
	public ResponseEntity<ApiResponse<BookReviewResponseDto>> getReview(
	        Authentication authentication,
	        @PathVariable Long reviewId
	) {
	    Long memberId = (Long) authentication.getPrincipal();
	    return ResponseEntity.ok(ApiResponse.success(bookReviewService.getReview(memberId, reviewId)));
	}

	// 독후감 작성
	@PostMapping
	public ResponseEntity<ApiResponse<BookReviewResponseDto>> createReview(
	        Authentication authentication,
	        @Valid @RequestBody BookReviewRequestDto requestDto
	) {
	    Long memberId = (Long) authentication.getPrincipal();
	    return ResponseEntity.ok(ApiResponse.success(bookReviewService.createReview(memberId, requestDto)));
	}

	// 독후감 수정
	@PatchMapping("/{reviewId}")
	public ResponseEntity<ApiResponse<BookReviewResponseDto>> updateReview(
	        Authentication authentication,
	        @PathVariable Long reviewId,
	        @Valid @RequestBody BookReviewRequestDto requestDto
	) {
	    Long memberId = (Long) authentication.getPrincipal();
	    return ResponseEntity.ok(ApiResponse.success(bookReviewService.updateReview(memberId, reviewId, requestDto)));
	}

	// 독후감 삭제
	@DeleteMapping("/{reviewId}")
	public ResponseEntity<ApiResponse<Void>> deleteReview(
	        Authentication authentication,
	        @PathVariable Long reviewId
	) {
	    Long memberId = (Long) authentication.getPrincipal();
	    bookReviewService.deleteReview(memberId, reviewId);
	    return ResponseEntity.ok(ApiResponse.success(null));
	}

	// 임시 저장
	@PatchMapping("/{reviewId}/draft")
	public ResponseEntity<ApiResponse<BookReviewResponseDto>> saveDraft(
	        Authentication authentication,
	        @PathVariable Long reviewId,
	        @Valid @RequestBody BookReviewRequestDto requestDto
	) {
	    Long memberId = (Long) authentication.getPrincipal();
	    return ResponseEntity.ok(ApiResponse.success(bookReviewService.saveDraft(memberId, reviewId, requestDto)));
	}

	// AI 피드백 조회
	@GetMapping("/{reviewId}/ai-feedback")
	public ResponseEntity<ApiResponse<AiFeedbackResponseDto>> getAiFeedback(
	        Authentication authentication,
	        @PathVariable Long reviewId
	) {
	    Long memberId = (Long) authentication.getPrincipal();
	    return ResponseEntity.ok(ApiResponse.success(bookReviewService.getAiFeedback(memberId, reviewId)));
	}

	// AI 피드백 요청
	@PostMapping("/{reviewId}/ai-feedback")
	public ResponseEntity<ApiResponse<AiFeedbackResponseDto>> requestAiFeedback(
	        Authentication authentication,
	        @PathVariable Long reviewId
	) {
	    Long memberId = (Long) authentication.getPrincipal();
	    return ResponseEntity.ok(ApiResponse.success(bookReviewService.requestAiFeedback(memberId, reviewId)));
	}
	
}
