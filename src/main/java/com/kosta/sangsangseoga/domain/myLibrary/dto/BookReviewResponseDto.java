package com.kosta.sangsangseoga.domain.myLibrary.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookReviewResponseDto {
	
	private Long reviewId;
	private Long bookId;
	private String content;
	private Boolean isDraft;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
