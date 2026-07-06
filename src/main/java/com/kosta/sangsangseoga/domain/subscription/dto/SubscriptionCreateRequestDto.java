package com.kosta.sangsangseoga.domain.subscription.dto;

import com.kosta.sangsangseoga.domain.subscription.enums.PlanType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SubscriptionCreateRequestDto {

    private PlanType planType;
    // Mock 결제: 토스페이먼츠 결제위젯이 돌려주는 값 형태만 맞춰 받는다. 실제 PG 서버 검증은 하지 않는다.
    private String paymentKey;
    private String orderId;
    private Integer amount;
}
