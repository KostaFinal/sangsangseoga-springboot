package com.kosta.sangsangseoga.domain.myLibrary.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReadingBookResponseDto {
	
	private Long bookId;	
	private String title;
    private String category;
    private String description;
    private String coverImageUrl;
	private Integer currentPage;
	private Integer progress;
	private Integer pageCount;
	private String bookType;
}
