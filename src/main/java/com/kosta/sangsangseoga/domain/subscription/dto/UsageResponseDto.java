package com.kosta.sangsangseoga.domain.subscription.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UsageResponseDto {

    private String plan;

    // PREMIUM 전용
    private Integer dailyTextRemaining;
    private Integer dailyTextLimit;
    private Integer dailyImageRemaining;
    private Integer dailyImageLimit;

    // FREE 전용
    private Boolean freeTrialUsed;
    private Integer trialPageLimit;
}
