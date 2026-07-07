package com.kosta.sangsangseoga.domain.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookDetailDto {

    private Long id;
    private String title;
    private String author;
    private Long authorId;
    private String bookType;
    private String coverImageUrl;
    private String description;
    private Integer pageCount;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isLikedByMe;
    private Boolean isBookmarkedByMe;
    private LocalDateTime createdAt;
}