package com.kosta.sangsangseoga.domain.myLibrary.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReadingProgressRequestDto {
	
	private Integer currentPage;
	
	private Integer progress;
}
