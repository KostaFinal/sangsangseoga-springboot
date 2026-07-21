package com.kosta.sangsangseoga.domain.subscription.scheduler;

import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
import com.kosta.sangsangseoga.domain.subscription.SubscriptionPolicy;
import com.kosta.sangsangseoga.domain.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionScheduler {

    private final MemberRepository memberRepository;
    private final SubscriptionService subscriptionService;

    /**
     * [매일 00:00] 프리미엄 회원 구독 만료 처리(자동갱신 또는 FREE 전환) 및 일일 생성 한도 재충전.
     * API 호출로 즉시 반영 안 된 회원들을 위한 안전망 역할이다(FREE 회원은 대상 아님).
     * 이 메서드 자체는 트랜잭션이 없다 - 회원별 처리가 각자 REQUIRES_NEW로 독립된 트랜잭션을 열기
     * 때문에, 여기서 감싸면 한 회원의 DB 예외로 나머지 회원들의 커밋까지 영향받을 수 있다.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void runDailySubscriptionMaintenance() {
        reconcileExpiredMembers();
        resetDailyUsageForPremiumMembers();
    }

    private void reconcileExpiredMembers() {
        List<Long> expiredMemberIds = memberRepository
                .findBySubscriptionPlanInAndSubscriptionEndAtBefore(
                        SubscriptionPolicy.PREMIUM_PLAN_TYPES, LocalDateTime.now())
                .stream()
                .map(Member::getId)
                .collect(Collectors.toList());

        expiredMemberIds.forEach(memberId -> {
            try {
                subscriptionService.reconcileIfExpired(memberId);
            } catch (Exception e) {
                // 회원별로 독립된 트랜잭션(REQUIRES_NEW)이라 한 명의 DB 예외가 다른 회원 처리에 영향을 주지 않는다.
                log.error("회원 id={} 구독 정리 중 오류 발생", memberId, e);
            }
        });
        log.info("만료된 PREMIUM 회원 정리(자동갱신/다운그레이드) 완료 - {}건", expiredMemberIds.size());
    }

    private void resetDailyUsageForPremiumMembers() {
        List<Long> memberIds = memberRepository
                .findBySubscriptionPlanInAndLastTokenResetDateNot(SubscriptionPolicy.PREMIUM_PLAN_TYPES, LocalDate.now())
                .stream()
                .map(Member::getId)
                .collect(Collectors.toList());

        memberIds.forEach(memberId -> {
            try {
                subscriptionService.resetDailyUsage(memberId);
            } catch (Exception e) {
                log.error("회원 id={} 일일 사용량 리셋 중 오류 발생", memberId, e);
            }
        });
        log.info("PREMIUM 일일 사용량 리셋 완료 - {}건", memberIds.size());
    }
}
