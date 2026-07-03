package com.kosta.sangsangseoga.domain.myLibrary.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReadingBookResponseDto {
	
	private Long bookId;	
	private String title;
    private String category;
    private String genre;
    
	private Integer currentPage;
	private Integer progress;
	private Integer pageCount;
}
