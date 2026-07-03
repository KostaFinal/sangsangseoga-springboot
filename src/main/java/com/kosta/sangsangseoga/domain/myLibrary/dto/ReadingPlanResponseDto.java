package com.kosta.sangsangseoga.domain.myLibrary.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReadingPlanResponseDto {
	private Long planId;

    private Long bookId;
    private String bookTitle;
    private String category;
    private Long coverImageId;

    private LocalDate planDate;
    private Integer targetPage;
    private String memo;

    private Boolean isCompleted;
    private LocalDateTime completedAt;
}
