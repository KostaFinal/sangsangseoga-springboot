package com.kosta.sangsangseoga.domain.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookPublishResponseDto {
    private Long bookId;
    private String title;
    private String status;   // "PUBLISHED"
    private Integer pageCount;
}