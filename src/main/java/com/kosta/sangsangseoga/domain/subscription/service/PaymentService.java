package com.kosta.sangsangseoga.domain.subscription.service;

import com.kosta.sangsangseoga.domain.subscription.dto.PaymentResponseDto;
import com.kosta.sangsangseoga.domain.subscription.entity.Payment;
import com.kosta.sangsangseoga.domain.subscription.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    // Mock 결제라 실제 카드 정보가 없어 인보이스에 표시할 사업자 정보는 고정값으로 내려준다.
    private static final String MERCHANT_NAME = "상상서가";
    private static final String MERCHANT_BUSINESS_NUMBER = "000-00-00000";
    private static final String MERCHANT_ADDRESS = "서울특별시 강남구 테헤란로 000";

    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public Page<PaymentResponseDto> getPaymentHistory(Long memberId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByMember_IdOrderByCreatedAtDesc(memberId, pageable);
        return payments.map(this::toResponseDto);
    }

    private PaymentResponseDto toResponseDto(Payment payment) {
        return PaymentResponseDto.builder()
                .paymentId(payment.getId())
                .planType(payment.getPlanType().name())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .pgTransactionId(payment.getPgTransactionId())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .maskedCardNumber(maskCardNumber(payment.getPgTransactionId()))
                .merchantName(MERCHANT_NAME)
                .merchantBusinessNumber(MERCHANT_BUSINESS_NUMBER)
                .merchantAddress(MERCHANT_ADDRESS)
                .build();
    }

    /** Mock 결제라 실카드번호가 없다. pgTransactionId 뒷자리로 표시용 마스킹 값만 만든다. */
    private String maskCardNumber(String pgTransactionId) {
        if (pgTransactionId == null || pgTransactionId.length() < 4) {
            return "**** **** **** ****";
        }
        String last4 = pgTransactionId.substring(pgTransactionId.length() - 4).toUpperCase();
        return "**** **** **** " + last4;
    }
}
