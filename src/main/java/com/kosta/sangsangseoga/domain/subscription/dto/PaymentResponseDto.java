package com.kosta.sangsangseoga.domain.subscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentResponseDto {

    @Schema(description = "결제 ID")
    private Long paymentId;

    @Schema(description = "결제한 플랜", example = "PREMIUM_MONTHLY", allowableValues = {"FREE", "PREMIUM_MONTHLY", "PREMIUM_YEARLY"})
    private String planType;

    @Schema(description = "결제 금액(원)")
    private Integer amount;

    @Schema(description = "결제 상태", example = "SUCCESS", allowableValues = {"SUCCESS", "FAILED", "CANCELLED"})
    private String status;

    @Schema(description = "PG 거래 ID(Mock)")
    private String pgTransactionId;

    @Schema(description = "결제 완료 시각")
    private LocalDateTime paidAt;

    @Schema(description = "결제 요청 생성 시각")
    private LocalDateTime createdAt;

    // 인보이스 출력용. Mock 결제라 실카드번호는 없고, pgTransactionId 기반으로 표시용 마스킹 값만 만든다.
    @Schema(description = "인보이스 표시용 마스킹된 카드번호(Mock). 실제 카드 정보가 아니다.")
    private String maskedCardNumber;

    @Schema(description = "인보이스 표시용 가맹점명(고정값)")
    private String merchantName;

    @Schema(description = "인보이스 표시용 사업자등록번호(고정값)")
    private String merchantBusinessNumber;

    @Schema(description = "인보이스 표시용 사업장 주소(고정값)")
    private String merchantAddress;
}
