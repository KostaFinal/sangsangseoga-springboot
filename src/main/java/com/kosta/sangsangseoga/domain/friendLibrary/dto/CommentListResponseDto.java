package com.kosta.sangsangseoga.domain.friendLibrary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentListResponseDto {

    private List<CommentDto> items;
    private String nextCursor;
    private Boolean hasNext;
}