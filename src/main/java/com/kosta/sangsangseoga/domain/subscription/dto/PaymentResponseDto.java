package com.kosta.sangsangseoga.domain.subscription.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentResponseDto {

    private Long paymentId;
    private String planType;
    private Integer amount;
    private String status;
    private String pgTransactionId;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    // 인보이스 출력용. Mock 결제라 실카드번호는 없고, pgTransactionId 기반으로 표시용 마스킹 값만 만든다.
    private String maskedCardNumber;
    private String merchantName;
    private String merchantBusinessNumber;
    private String merchantAddress;
}
