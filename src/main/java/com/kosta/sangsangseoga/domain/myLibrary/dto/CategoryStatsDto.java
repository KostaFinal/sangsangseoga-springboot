package com.kosta.sangsangseoga.domain.myLibrary.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryStatsDto {
	private String category;
    private Long count;
}
