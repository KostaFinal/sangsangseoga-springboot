package com.kosta.sangsangseoga.domain.myLibrary.dto;

import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.myLibrary.entity.MyReading;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WishlistBookResponseDto {
	private Long bookId;
    private String title;
    private String description;
    private String category;
    private Long coverImageId; // 나중에 지울 수도
	
    public static WishlistBookResponseDto from(MyReading myReading) {

        Book book = myReading.getBook();

        return WishlistBookResponseDto.builder()
                .bookId(book.getId())
                .title(book.getTitle())
                .description(book.getDescription())
                .category(book.getCategory())
                .coverImageId(book.getCoverImageId())// 나중에 지울 수도
                .build();
    }
	
}
