package com.kosta.sangsangseoga.domain.myLibrary.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WishlistBookResponseDto {
	private Long bookId;
    private String title;
    private String description;
    private String category;
    private String bookType;
    private String coverImageUrl;
    private Integer pageCount;
	
}
