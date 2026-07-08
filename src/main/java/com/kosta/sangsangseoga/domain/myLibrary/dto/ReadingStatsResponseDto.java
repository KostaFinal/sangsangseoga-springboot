package com.kosta.sangsangseoga.domain.myLibrary.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReadingStatsResponseDto {

	private Long totalReadingTime;
	private Long totalPagesRead;
	private Long reportCount;

	private Long wishlistBookCount;
	private Long readingBookCount;
	private Long completedBookCount;

	private List<CategoryStatsDto> categoryStats;
	private List<FinishedBookResponseDto> finishedBooks;
	
	private Long writtenBookCount;
	private List<CategoryStatsDto> writtenCategoryStats;
}
