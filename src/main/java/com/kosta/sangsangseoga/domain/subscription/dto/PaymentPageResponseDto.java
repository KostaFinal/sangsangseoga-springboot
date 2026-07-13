package com.kosta.sangsangseoga.domain.subscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PaymentPageResponseDto {

    @Schema(description = "결제 내역 목록(최신순)")
    private List<PaymentResponseDto> items;

    @Schema(description = "전체 결제 건수")
    private Long totalCount;

    @Schema(description = "현재 페이지(0부터 시작)")
    private Integer page;

    @Schema(description = "다음 페이지 존재 여부")
    private Boolean hasNext;
}
