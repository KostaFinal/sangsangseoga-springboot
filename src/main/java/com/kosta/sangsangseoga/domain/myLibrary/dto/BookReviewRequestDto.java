package com.kosta.sangsangseoga.domain.myLibrary.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BookReviewRequestDto {
	
	//독후감 내용
	private String content;
	
	//임시저장 여부
	private Boolean isDraft;
}
