package com.kosta.sangsangseoga.domain.myLibrary.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BookReviewRequestDto {
	
	private Long bookId;
	private String content;
	private Boolean isDraft;
}
