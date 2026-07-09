package com.kosta.sangsangseoga.domain.myLibrary.dto;

import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BookReviewRequestDto {
	
	 public interface OnPublish {}

	    private Long bookId;

	    @NotBlank(message = "독후감 내용은 필수입니다.", groups = OnPublish.class)
	    private String content;

	    private Boolean isDraft;
}
