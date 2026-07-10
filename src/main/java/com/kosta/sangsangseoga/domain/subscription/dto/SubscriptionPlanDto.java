package com.kosta.sangsangseoga.domain.subscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubscriptionPlanDto {

    @Schema(description = "플랜 종류", example = "PREMIUM_MONTHLY", allowableValues = {"FREE", "PREMIUM_MONTHLY", "PREMIUM_YEARLY"})
    private String planType;

    @Schema(description = "가격(원). FREE는 0.")
    private Integer price;

    @Schema(description = "PREMIUM 전용 일일 텍스트 생성 한도. FREE 플랜에는 null.", nullable = true)
    private Integer dailyTextLimit;

    @Schema(description = "PREMIUM 전용 일일 이미지 생성 한도. FREE 플랜에는 null.", nullable = true)
    private Integer dailyImageLimit;

    @Schema(description = "FREE 전용 생애 1회 무료체험 최대 페이지 수. PREMIUM 플랜에는 null.", nullable = true)
    private Integer trialPageLimit;
}
