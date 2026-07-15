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
     * [매일 00:00] 프리미엄 회원 구독 만료 처리(자동갱신 또는 FREE 전환) 및 일일 생성 한도 재충전.
     * API 호출로 즉시 반영 안 된 회원들을 위한 안전망 역할이다(FREE 회원은 대상 아님).
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

        expiredMembers.forEach(member -> {
            try {
                subscriptionService.reconcileIfExpired(member);
            } catch (Exception e) {
                // 한 회원 처리 중 예외가 나도 나머지는 계속 진행한다. 단, DB 레벨 예외로 트랜잭션이
                // rollback-only로 마킹되는 경우(Payment가 IDENTITY라 save() 즉시 INSERT됨)까지는 못 막는다.
                log.error("회원 id={} 구독 정리 중 오류 발생", member.getId(), e);
            }
        });
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
