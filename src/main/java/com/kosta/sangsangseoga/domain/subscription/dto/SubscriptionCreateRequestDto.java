package com.kosta.sangsangseoga.domain.subscription.dto;

import com.kosta.sangsangseoga.domain.subscription.enums.PlanType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class SubscriptionCreateRequestDto {

    @Schema(description = "구독할 플랜. FREE는 결제 대상이 아니라 PREMIUM_MONTHLY/PREMIUM_YEARLY만 허용된다.",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "planType은 필수입니다.")
    private PlanType planType;

    @Schema(description = "Mock 결제: 실제 PG 서버 검증은 하지 않고, 결제위젯이 돌려주는 값 형태만 그대로 받는다.", nullable = true)
    private String paymentKey;

    @Schema(description = "Mock 결제: 주문 식별자", nullable = true)
    private String orderId;

    @Schema(description = "Mock 결제: 클라이언트가 보낸 금액은 신뢰하지 않고, 서버가 planType 기준으로 다시 계산한다.", nullable = true)
    private Integer amount;
}
