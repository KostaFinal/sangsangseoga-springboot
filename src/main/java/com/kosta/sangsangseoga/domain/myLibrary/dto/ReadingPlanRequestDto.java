package com.kosta.sangsangseoga.domain.myLibrary.dto;

import java.time.LocalDate;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReadingPlanRequestDto {
	private Long bookId;
    private LocalDate planDate;
    private Integer targetPage;
    private String memo;
}
