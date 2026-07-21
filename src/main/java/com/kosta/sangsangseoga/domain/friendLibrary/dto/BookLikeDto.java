package com.kosta.sangsangseoga.domain.friendLibrary.dto;
 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookLikeDto {
 
    private Long bookId;
    private Integer likeCount;
    private Boolean isLikedByMe;
}
 