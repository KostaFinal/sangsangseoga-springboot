package com.kosta.sangsangseoga.domain.myLibrary.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BookReviewRequestDto {
	
	private Long bookId;
	private String content;
	private Boolean isDraft;
}
