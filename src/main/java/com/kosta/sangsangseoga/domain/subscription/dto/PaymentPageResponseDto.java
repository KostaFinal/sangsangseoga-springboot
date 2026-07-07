package com.kosta.sangsangseoga.domain.subscription.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PaymentPageResponseDto {

    private List<PaymentResponseDto> items;
    private Long totalCount;
    private Integer page;
    private Boolean hasNext;
}
