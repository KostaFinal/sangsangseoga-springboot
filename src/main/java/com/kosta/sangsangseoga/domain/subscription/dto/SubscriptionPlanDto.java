package com.kosta.sangsangseoga.domain.subscription.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubscriptionPlanDto {

    private String planType;
    private Integer price;
    private Integer dailyTextLimit;
    private Integer dailyImageLimit;
    private Integer trialPageLimit;
}
