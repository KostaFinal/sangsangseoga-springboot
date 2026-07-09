package com.kosta.sangsangseoga.domain.myLibrary.dto;

import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BookReviewRequestDto {
	
	private Long bookId;
	
	@NotBlank(message = "독후감 내용은 필수입니다.")
	private String content;
	private Boolean isDraft;
}
