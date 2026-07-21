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
    private String description;
    private String coverImageUrl;
    private LocalDateTime startedAt;
	private LocalDateTime completedAt;
	private Integer readingTime;
	private String readingStatus;
	private Integer rereadCount;
	private String bookType;
	private Integer pageCount;
	private Integer viewCount;
	private Integer likeCount;
}
