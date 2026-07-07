package com.kosta.sangsangseoga.domain.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminReportListResponseDto {

    private List<AdminReportListItemDto> items;
    private Long totalCount;
    private Integer page;
    private Boolean hasNext;
}
