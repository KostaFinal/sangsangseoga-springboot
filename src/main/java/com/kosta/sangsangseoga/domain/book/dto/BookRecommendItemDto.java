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
    private String bookType;
    private String coverImageUrl;
    private String description;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
}