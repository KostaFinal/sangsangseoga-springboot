package com.kosta.sangsangseoga.domain.friendLibrary.dto;
 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookmarkDto {
 
    private Long bookId;
    private Integer pageNo;
    private Boolean isBookmarkedByMe;
}