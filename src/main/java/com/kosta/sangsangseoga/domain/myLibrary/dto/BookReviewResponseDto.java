package com.kosta.sangsangseoga.domain.myLibrary.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookReviewResponseDto {
	
	private Long reviewId;
	private Long bookId;
	private String bookTitle;
    private Long coverImageId;
	private String content;
	private Boolean isDraft;
	private String aiFeedbackContent;
    private LocalDateTime aiFeedbackCreatedAt;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
