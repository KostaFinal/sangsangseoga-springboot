package com.kosta.sangsangseoga.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminActionLogListResponseDto {

    @Schema(description = "관리자 처리 이력 목록(최신순)")
    private List<AdminActionLogListItemDto> items;

    @Schema(description = "전체 이력 건수")
    private Long totalCount;

    @Schema(description = "현재 페이지(0부터 시작)")
    private Integer page;

    @Schema(description = "다음 페이지 존재 여부")
    private Boolean hasNext;
}
