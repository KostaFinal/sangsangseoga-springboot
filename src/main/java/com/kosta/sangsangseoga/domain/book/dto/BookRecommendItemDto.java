package com.kosta.sangsangseoga.domain.book.dto;
 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRecommendItemDto {
 
    private Long id;
    private String title;
    private String author;
    private String genre;
    private String coverImageUrl;
    private String summary;
}