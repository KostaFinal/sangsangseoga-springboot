package com.kosta.sangsangseoga.domain.myLibrary.dto;

import javax.validation.constraints.Min;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReadingProgressRequestDto {
	
	private Integer currentPage;
	
	private Integer progress;
	
	@Min(value = 0, message = "독서 시간은 0 이상이어야 합니다.")
	private Integer readingTime;
}
