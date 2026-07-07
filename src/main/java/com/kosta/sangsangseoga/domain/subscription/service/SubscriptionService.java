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

    /**
     * 조회만 하는 API지만 만료 정리(reconcileIfExpired)에서 쓰기가 발생할 수 있어
     * readOnly로 두지 않는다.
     */
    public SubscriptionMeResponseDto getMySubscription(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));
        reconcileIfExpired(member);
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
        reconcileIfExpired(member);

        PlanType planType = request.getPlanType();
        if (planType == null || !planType.isPremium()) {
            throw new CustomException(SubscriptionErrorCode.UNSUPPORTED_PLAN_TYPE);
        }
        if (member.getSubscriptionPlan().isPremium()) {
            // 이미 PREMIUM이면 autoRenew 여부와 무관하게 새 결제를 만들지 않는다.
            // 해지 예약(autoRenew=false) 상태라면 재결제 없이 resume()으로 되돌려야 한다.
            throw new CustomException(SubscriptionErrorCode.ALREADY_PREMIUM_MEMBER);
        }

        chargeAndStartPremium(member, planType, request.getPaymentKey());
        return toMeResponseDto(member);
    }

    /**
     * 월간 -> 연간 즉시 전환(재결제, 남은 월간 기간은 소멸). 연간에서 월간으로의 다운그레이드는
     * 이미 결제한 잔여 기간을 환불 없이 날리게 되어 불공평하므로 이 API로는 지원하지 않는다
     * (해지 예약 후 만료를 기다렸다가 월간으로 재구독하는 기존 흐름을 이용해야 한다).
     */
    public SubscriptionMeResponseDto changePlan(Long memberId, SubscriptionCreateRequestDto request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));
        reconcileIfExpired(member);

        if (!member.getSubscriptionPlan().isPremium()) {
            throw new CustomException(SubscriptionErrorCode.NOT_PREMIUM_MEMBER);
        }

        PlanType requestedPlanType = request.getPlanType();
        if (requestedPlanType != PlanType.PREMIUM_YEARLY) {
            throw new CustomException(SubscriptionErrorCode.DOWNGRADE_NOT_SUPPORTED);
        }
        if (member.getSubscriptionPlan() == PlanType.PREMIUM_YEARLY) {
            throw new CustomException(SubscriptionErrorCode.ALREADY_YEARLY_PLAN);
        }

        chargeAndStartPremium(member, PlanType.PREMIUM_YEARLY, request.getPaymentKey());
        return toMeResponseDto(member);
    }

    private void chargeAndStartPremium(Member member, PlanType planType, String paymentKey) {
        int price = SubscriptionPolicy.priceOf(planType);
        String pgTransactionId = paymentKey != null ? paymentKey : UUID.randomUUID().toString();

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
    }

    /**
     * 해지 예약. 결제 주기(subscriptionEndAt)까지는 PREMIUM을 유지하고,
     * 만료 시점에 스케줄러(SubscriptionScheduler)가 FREE로 전환한다.
     */
    public SubscriptionMeResponseDto cancelSubscription(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));
        reconcileIfExpired(member);

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
        reconcileIfExpired(member);

        if (!member.getSubscriptionPlan().isPremium()) {
            throw new CustomException(SubscriptionErrorCode.NOT_PREMIUM_MEMBER);
        }
        if (Boolean.TRUE.equals(member.getSubscriptionAutoRenew())) {
            throw new CustomException(SubscriptionErrorCode.SUBSCRIPTION_NOT_CANCELLED);
        }

        member.resumeAutoRenew();
        return toMeResponseDto(member);
    }

    /**
     * subscriptionEndAt이 이미 지났는데 자정 배치(SubscriptionScheduler)가 아직 처리하지 않은 회원을
     * 그 자리에서 즉시 정리한다. 상태 필드(subscriptionPlan/autoRenew)와 실제 만료일이라는 두 진실
     * 소스가 배치 주기 사이에 어긋나는 걸 막기 위해, 구독을 조회/변경하는 모든 진입점에서 먼저 호출한다.
     * 배치와 정확히 같은 규칙(자동갱신 또는 다운그레이드)을 쓴다 — SubscriptionScheduler도 이 메서드를 그대로 쓴다.
     */
    public void reconcileIfExpired(Member member) {
        if (!member.getSubscriptionPlan().isPremium()) {
            return;
        }
        if (member.getSubscriptionEndAt() == null || !member.getSubscriptionEndAt().isBefore(LocalDateTime.now())) {
            return;
        }

        if (Boolean.TRUE.equals(member.getSubscriptionAutoRenew())) {
            PlanType planType = member.getSubscriptionPlan();
            Payment renewalPayment = Payment.builder()
                    .member(member)
                    .amount(SubscriptionPolicy.priceOf(planType))
                    .status(PaymentStatus.SUCCESS)
                    .planType(planType)
                    .pgTransactionId(UUID.randomUUID().toString())
                    .paidAt(LocalDateTime.now())
                    .build();
            paymentRepository.save(renewalPayment);

            LocalDateTime startAt = LocalDateTime.now();
            LocalDateTime endAt = startAt.plusDays(SubscriptionPolicy.periodDaysOf(planType));
            member.renewPremiumSubscription(startAt, endAt,
                    SubscriptionPolicy.PREMIUM_DAILY_TEXT_LIMIT, SubscriptionPolicy.PREMIUM_DAILY_IMAGE_LIMIT);
        } else {
            member.downgradeToFree();
        }
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
