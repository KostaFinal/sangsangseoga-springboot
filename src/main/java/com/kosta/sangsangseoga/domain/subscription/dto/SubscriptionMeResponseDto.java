package com.kosta.sangsangseoga.domain.subscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class SubscriptionMeResponseDto {

    @Schema(description = "현재 플랜", example = "FREE", allowableValues = {"FREE", "PREMIUM_MONTHLY", "PREMIUM_YEARLY"})
    private String planType;

    // Boolean(래퍼)로 선언해야 Lombok이 getIsPremium()을 생성해 Jackson이 "isPremium"으로 직렬화한다.
    // primitive boolean이었다면 isPremium()이 생성되어 Jackson이 "is"를 떼고 "premium"으로 내보낸다.
    @Schema(description = "PREMIUM_MONTHLY/YEARLY 여부")
    private Boolean isPremium;

    @Schema(description = "true면 해지 예약 상태(benefitEndDate까지는 계속 이용 가능, 이후 자동으로 FREE 전환)")
    private Boolean isCanceled;

    @Schema(description = "현재 결제 주기의 혜택 종료일. FREE 회원은 null.", nullable = true)
    private LocalDate benefitEndDate;

    @Schema(description = "다음 자동 결제 예정일. isCanceled=true거나 FREE 회원이면 null.", nullable = true)
    private LocalDate nextBillingDate;
}
