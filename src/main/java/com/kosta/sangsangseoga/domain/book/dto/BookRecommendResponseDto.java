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
public class BookRecommendResponseDto {

    private List<BookRecommendItemDto> items;
}