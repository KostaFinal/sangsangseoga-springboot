package com.kosta.sangsangseoga.domain.myLibrary.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FinishedBookResponseDto {
	
	private Long bookId;
	private String title;
    private String category;
    
	private LocalDateTime completedAt;
	private Integer readingTime;
}
