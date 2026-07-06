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
public class AuthorListResponseDto {

    private List<AuthorListItemDto> items;
    private Long totalCount;
    private Integer page;
    private Boolean hasNext;
}
