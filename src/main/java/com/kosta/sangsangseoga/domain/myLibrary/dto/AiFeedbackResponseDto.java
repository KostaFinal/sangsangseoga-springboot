package com.kosta.sangsangseoga.domain.myLibrary.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiFeedbackResponseDto {
	
	//AI 피드백 내용
	private String aiFeedbackContent;
	
	//AI 피드백 생성일
	private LocalDateTime aiFeedbackCreatedAt;
}
