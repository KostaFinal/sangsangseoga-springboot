package com.kosta.sangsangseoga.domain.subscription.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class SubscriptionMeResponseDto {

    private String planType;
    // Boolean(래퍼)로 선언해야 Lombok이 getIsPremium()을 생성해 Jackson이 "isPremium"으로 직렬화한다.
    // primitive boolean이었다면 isPremium()이 생성되어 Jackson이 "is"를 떼고 "premium"으로 내보낸다.
    private Boolean isPremium;
    private Boolean isCanceled;
    private LocalDate benefitEndDate;
    private LocalDate nextBillingDate;
}
