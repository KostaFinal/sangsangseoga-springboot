package com.kosta.sangsangseoga.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminMemberListResponseDto {

    @Schema(description = "회원 목록")
    private List<AdminMemberListItemDto> items;

    @Schema(description = "전체 회원 수(필터 적용 후)")
    private Long totalCount;

    @Schema(description = "현재 페이지(0부터 시작)")
    private Integer page;

    @Schema(description = "다음 페이지 존재 여부")
    private Boolean hasNext;
}
