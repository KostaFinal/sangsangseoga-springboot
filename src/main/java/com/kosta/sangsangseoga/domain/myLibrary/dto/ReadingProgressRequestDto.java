package com.kosta.sangsangseoga.domain.myLibrary.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReadingProgressRequestDto {
	
	private Integer currentPage;
	
	private Integer progress;
}
