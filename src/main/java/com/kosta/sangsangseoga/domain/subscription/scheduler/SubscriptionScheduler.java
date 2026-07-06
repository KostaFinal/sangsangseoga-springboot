package com.kosta.sangsangseoga.domain.subscription.scheduler;

import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
import com.kosta.sangsangseoga.domain.subscription.SubscriptionPolicy;
import com.kosta.sangsangseoga.domain.subscription.entity.Payment;
import com.kosta.sangsangseoga.domain.subscription.enums.PaymentStatus;
import com.kosta.sangsangseoga.domain.subscription.enums.PlanType;
import com.kosta.sangsangseoga.domain.subscription.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionScheduler {

    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;

    /**
     * 매일 00:00 실행.
     * 1) 결제 주기가 끝난 PREMIUM(월간/연간) 회원 중 자동갱신 켜진 회원은 Mock 재결제로 자동 연장(같은 요금제 유지)
     * 2) 자동갱신 꺼둔(해지 예약) 회원은 FREE로 전환
     * 3) PREMIUM 회원의 일일 텍스트/이미지 생성 한도를 재충전
     * FREE 회원은 일일 한도 개념이 없어(생애 1회 체험 방식) 이 배치 대상이 아니다.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void runDailySubscriptionMaintenance() {
        autoRenewExpiredMembers();
        downgradeExpiredCancelledMembers();
        resetDailyUsageForPremiumMembers();
    }

    private void autoRenewExpiredMembers() {
        List<Member> membersToRenew = memberRepository
                .findBySubscriptionPlanInAndSubscriptionAutoRenewTrueAndSubscriptionEndAtBefore(
                        SubscriptionPolicy.PREMIUM_PLAN_TYPES, LocalDateTime.now());

        for (Member member : membersToRenew) {
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
        }
        log.info("PREMIUM 자동갱신(Mock 재결제) 완료 - {}건", membersToRenew.size());
    }

    private void downgradeExpiredCancelledMembers() {
        List<Member> expiredMembers = memberRepository
                .findBySubscriptionPlanInAndSubscriptionAutoRenewFalseAndSubscriptionEndAtBefore(
                        SubscriptionPolicy.PREMIUM_PLAN_TYPES, LocalDateTime.now());

        expiredMembers.forEach(Member::downgradeToFree);
        log.info("해지 예약 만료 회원 FREE 전환 완료 - {}건", expiredMembers.size());
    }

    private void resetDailyUsageForPremiumMembers() {
        List<Member> membersToReset = memberRepository
                .findBySubscriptionPlanInAndLastTokenResetDateNot(SubscriptionPolicy.PREMIUM_PLAN_TYPES, LocalDate.now());

        membersToReset.forEach(member -> member.resetDailyUsage(
                SubscriptionPolicy.PREMIUM_DAILY_TEXT_LIMIT, SubscriptionPolicy.PREMIUM_DAILY_IMAGE_LIMIT));
        log.info("PREMIUM 일일 사용량 리셋 완료 - {}건", membersToReset.size());
    }
}
