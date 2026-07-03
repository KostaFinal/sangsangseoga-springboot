package com.kosta.sangsangseoga.domain.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookListResponseDto {

    private List<BookListItemDto> items;
    private Long totalCount;
    private Integer page;
    private Boolean hasNext;
}