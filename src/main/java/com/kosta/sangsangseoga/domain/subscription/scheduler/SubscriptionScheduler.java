package com.kosta.sangsangseoga.domain.subscription.scheduler;

import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
import com.kosta.sangsangseoga.domain.subscription.SubscriptionPolicy;
import com.kosta.sangsangseoga.domain.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionScheduler {

    private final MemberRepository memberRepository;
    private final SubscriptionService subscriptionService;

    /**
     * 매일 00:00 실행. 실제로는 안전망 역할이 크다 — 만료 시점에 회원이 구독 관련 API를 직접
     * 호출하면 SubscriptionService.reconcileIfExpired가 그 자리에서 이미 처리해주기 때문에,
     * 이 배치는 그 사이 아무 API도 호출하지 않은 회원들을 하루 단위로 쓸어서 정리하는 역할이다.
     * 1) 결제 주기가 끝난 PREMIUM(월간/연간) 회원을 자동갱신(Mock 재결제) 또는 FREE 전환으로 정리
     * 2) PREMIUM 회원의 일일 텍스트/이미지 생성 한도를 재충전
     * FREE 회원은 일일 한도 개념이 없어(생애 1회 체험 방식) 이 배치 대상이 아니다.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void runDailySubscriptionMaintenance() {
        reconcileExpiredMembers();
        resetDailyUsageForPremiumMembers();
    }

    private void reconcileExpiredMembers() {
        List<Member> expiredMembers = memberRepository
                .findBySubscriptionPlanInAndSubscriptionEndAtBefore(
                        SubscriptionPolicy.PREMIUM_PLAN_TYPES, LocalDateTime.now());

        expiredMembers.forEach(subscriptionService::reconcileIfExpired);
        log.info("만료된 PREMIUM 회원 정리(자동갱신/다운그레이드) 완료 - {}건", expiredMembers.size());
    }

    private void resetDailyUsageForPremiumMembers() {
        List<Member> membersToReset = memberRepository
                .findBySubscriptionPlanInAndLastTokenResetDateNot(SubscriptionPolicy.PREMIUM_PLAN_TYPES, LocalDate.now());

        membersToReset.forEach(member -> member.resetDailyUsage(
                SubscriptionPolicy.PREMIUM_DAILY_TEXT_LIMIT, SubscriptionPolicy.PREMIUM_DAILY_IMAGE_LIMIT));
        log.info("PREMIUM 일일 사용량 리셋 완료 - {}건", membersToReset.size());
    }
}
