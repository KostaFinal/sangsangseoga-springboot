package com.kosta.sangsangseoga.domain.book.dto;

import com.kosta.sangsangseoga.domain.book.enums.AgeGroup;
import com.kosta.sangsangseoga.domain.book.enums.BookType;
import com.kosta.sangsangseoga.domain.book.enums.CreationMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.time.LocalDateTime;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookListDto {
 
    private Long id;
    private Long memberId;
    private BookType bookType;
    private CreationMode creationMode;
    private AgeGroup authorAgeGroup;
    private AgeGroup readerAgeGroup;
    private String title;
    private String description;
    private String summary;
    private String category;
    private String genre;
    private Long coverImageId;
    private String status;
    private Integer pageCount;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
