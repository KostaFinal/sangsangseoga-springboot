package com.kosta.sangsangseoga.domain.subscription.service;

import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
import com.kosta.sangsangseoga.domain.subscription.SubscriptionPolicy;
import com.kosta.sangsangseoga.domain.subscription.dto.SubscriptionCreateRequestDto;
import com.kosta.sangsangseoga.domain.subscription.dto.SubscriptionMeResponseDto;
import com.kosta.sangsangseoga.domain.subscription.dto.SubscriptionPlanDto;
import com.kosta.sangsangseoga.domain.subscription.entity.Payment;
import com.kosta.sangsangseoga.domain.subscription.enums.PaymentStatus;
import com.kosta.sangsangseoga.domain.subscription.enums.PlanType;
import com.kosta.sangsangseoga.domain.subscription.exception.SubscriptionErrorCode;
import com.kosta.sangsangseoga.domain.subscription.repository.PaymentRepository;
import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionService {

    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public List<SubscriptionPlanDto> getPlans() {
        return List.of(
                SubscriptionPlanDto.builder()
                        .planType(PlanType.FREE.name())
                        .price(0)
                        .trialPageLimit(SubscriptionPolicy.FREE_TRIAL_PAGE_LIMIT)
                        .build(),
                SubscriptionPlanDto.builder()
                        .planType(PlanType.PREMIUM_MONTHLY.name())
                        .price(SubscriptionPolicy.PREMIUM_MONTHLY_PRICE)
                        .dailyTextLimit(SubscriptionPolicy.PREMIUM_DAILY_TEXT_LIMIT)
                        .dailyImageLimit(SubscriptionPolicy.PREMIUM_DAILY_IMAGE_LIMIT)
                        .build(),
                SubscriptionPlanDto.builder()
                        .planType(PlanType.PREMIUM_YEARLY.name())
                        .price(SubscriptionPolicy.PREMIUM_YEARLY_PRICE)
                        .dailyTextLimit(SubscriptionPolicy.PREMIUM_DAILY_TEXT_LIMIT)
                        .dailyImageLimit(SubscriptionPolicy.PREMIUM_DAILY_IMAGE_LIMIT)
                        .build()
        );
    }

    @Transactional(readOnly = true)
    public SubscriptionMeResponseDto getMySubscription(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));
        return toMeResponseDto(member);
    }

    /**
     * 정기구독 시작(결제 승인 콜백 이후 단일 호출). 실제 PG 연동 전이라 paymentKey/orderId는
     * 형태만 받아서 pgTransactionId로 남기고, 토스 서버 검증 없이 바로 SUCCESS 처리한다.
     * 금액은 클라이언트 값을 신뢰하지 않고 서버가 planType 기준으로 다시 계산한다.
     */
    public SubscriptionMeResponseDto subscribe(Long memberId, SubscriptionCreateRequestDto request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        PlanType planType = request.getPlanType();
        if (planType == null || !planType.isPremium()) {
            throw new CustomException(SubscriptionErrorCode.UNSUPPORTED_PLAN_TYPE);
        }
        if (member.getSubscriptionPlan().isPremium()) {
            // 이미 PREMIUM이면 autoRenew 여부와 무관하게 새 결제를 만들지 않는다.
            // 해지 예약(autoRenew=false) 상태라면 재결제 없이 resume()으로 되돌려야 한다.
            throw new CustomException(SubscriptionErrorCode.ALREADY_PREMIUM_MEMBER);
        }

        int price = SubscriptionPolicy.priceOf(planType);
        String pgTransactionId = request.getPaymentKey() != null
                ? request.getPaymentKey()
                : UUID.randomUUID().toString();

        Payment payment = Payment.builder()
                .member(member)
                .amount(price)
                .status(PaymentStatus.SUCCESS)
                .planType(planType)
                .pgTransactionId(pgTransactionId)
                .paidAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        LocalDateTime startAt = LocalDateTime.now();
        LocalDateTime endAt = startAt.plusDays(SubscriptionPolicy.periodDaysOf(planType));
        member.startPremiumSubscription(planType, startAt, endAt,
                SubscriptionPolicy.PREMIUM_DAILY_TEXT_LIMIT, SubscriptionPolicy.PREMIUM_DAILY_IMAGE_LIMIT);

        return toMeResponseDto(member);
    }

    /**
     * 해지 예약. 결제 주기(subscriptionEndAt)까지는 PREMIUM을 유지하고,
     * 만료 시점에 스케줄러(SubscriptionScheduler)가 FREE로 전환한다.
     */
    public SubscriptionMeResponseDto cancelSubscription(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        if (!member.getSubscriptionPlan().isPremium()) {
            throw new CustomException(SubscriptionErrorCode.NOT_PREMIUM_MEMBER);
        }
        if (!Boolean.TRUE.equals(member.getSubscriptionAutoRenew())) {
            throw new CustomException(SubscriptionErrorCode.SUBSCRIPTION_ALREADY_CANCELLED);
        }

        member.reserveCancellation();
        return toMeResponseDto(member);
    }

    /**
     * 해지 예약 취소(재개). 아직 혜택 기간이 남아있을 때 결제 없이 autoRenew만 다시 켠다.
     */
    public SubscriptionMeResponseDto resumeSubscription(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        if (!member.getSubscriptionPlan().isPremium()) {
            throw new CustomException(SubscriptionErrorCode.NOT_PREMIUM_MEMBER);
        }
        if (Boolean.TRUE.equals(member.getSubscriptionAutoRenew())) {
            throw new CustomException(SubscriptionErrorCode.SUBSCRIPTION_NOT_CANCELLED);
        }

        member.resumeAutoRenew();
        return toMeResponseDto(member);
    }

    private SubscriptionMeResponseDto toMeResponseDto(Member member) {
        boolean isPremium = member.getSubscriptionPlan().isPremium();
        boolean isCanceled = isPremium && !Boolean.TRUE.equals(member.getSubscriptionAutoRenew());
        boolean willAutoRenew = isPremium && !isCanceled;

        return SubscriptionMeResponseDto.builder()
                .planType(member.getSubscriptionPlan().name())
                .isPremium(isPremium)
                .isCanceled(isCanceled)
                .benefitEndDate(member.getSubscriptionEndAt() != null ? member.getSubscriptionEndAt().toLocalDate() : null)
                .nextBillingDate(willAutoRenew && member.getSubscriptionEndAt() != null
                        ? member.getSubscriptionEndAt().toLocalDate() : null)
                .build();
    }
}
