package com.kosta.sangsangseoga.domain.myLibrary.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MyReportHistoryResponseDto {
	
	// 현재 페이지의 신고 내역
    private List<MyReportHistoryItemDto> items;

    // 전체 신고 건수
    private Long totalCount;

    // 현재 페이지, 0부터 시작
    private Integer page;

    // 다음 페이지 존재 여부
    private Boolean hasNext;
}
