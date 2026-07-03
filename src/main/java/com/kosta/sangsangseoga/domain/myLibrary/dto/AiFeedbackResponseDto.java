package com.kosta.sangsangseoga.domain.myLibrary.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiFeedbackResponseDto {
	
	private Long reviewId;
    private String aiFeedbackContent;
    private LocalDateTime aiFeedbackCreatedAt;
}
