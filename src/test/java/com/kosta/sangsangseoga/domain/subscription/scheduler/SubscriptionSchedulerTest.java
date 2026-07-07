package com.kosta.sangsangseoga.domain.subscription.scheduler;

import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
import com.kosta.sangsangseoga.domain.subscription.SubscriptionPolicy;
import com.kosta.sangsangseoga.domain.subscription.enums.PlanType;
import com.kosta.sangsangseoga.domain.subscription.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionSchedulerTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private SubscriptionScheduler subscriptionScheduler;

    @Test
    void 만료된_프리미엄_회원은_reconcileIfExpired가_호출된다 () {
        // given
        Member expiredMember1 = Member.builder().email("aaa@test.com").password("aaa111").build();
        Member expiredMember2 = Member.builder().email("bbb@test.com").password("bbb222").build();
        // 프리미엄 플랜이고 어느 LocalDateTime 이전의 멤버 조회시 -> 위의 2개 멤버 리스트를 반환하도록 설정한다.
        when(memberRepository.findBySubscriptionPlanInAndSubscriptionEndAtBefore(
            eq(SubscriptionPolicy.PREMIUM_PLAN_TYPES), any(LocalDateTime.class)))
            .thenReturn(List.of(expiredMember1, expiredMember2));
        // 프리미엄 플랜이고 LocalDate가 아닌 멤버 조회 시 -> 위 2개 멤버 리스트 반환
        when(memberRepository.findBySubscriptionPlanInAndLastTokenResetDateNot(
            eq(SubscriptionPolicy.PREMIUM_PLAN_TYPES), any(LocalDate.class)))
            .thenReturn(List.of());

        // when
        subscriptionScheduler.runDailySubscriptionMaintenance();

        // then
        verify(subscriptionService).reconcileIfExpired(expiredMember1);
        verify(subscriptionService).reconcileIfExpired(expiredMember2);
    }

    @Test
    void 오늘_리셋_안된_프리미엄_회원은_일일_사용량이_재충전된다 () {
        // given
        Member member = Member.builder().email("ccc@test.com").password("ccc333").build();
        // 월간 구독 중이며, 오늘 일일 텍스트/이미지 잔여량을 모두 0으로 세팅
        member.startPremiumSubscription(PlanType.PREMIUM_MONTHLY,
            LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(29), 0, 0);
        // 만료된 회원 없음
        when(memberRepository.findBySubscriptionPlanInAndSubscriptionEndAtBefore(
            eq(SubscriptionPolicy.PREMIUM_PLAN_TYPES), any(LocalDateTime.class)))
            .thenReturn(List.of());
        //
        when(memberRepository.findBySubscriptionPlanInAndLastTokenResetDateNot(
            eq(SubscriptionPolicy.PREMIUM_PLAN_TYPES), any(LocalDate.class)))
            .thenReturn(List.of(member));

        // when
        subscriptionScheduler.runDailySubscriptionMaintenance();

        // then
        assertThat(member.getDailyTextRemaining()).isEqualTo(SubscriptionPolicy.PREMIUM_DAILY_TEXT_LIMIT);
        assertThat(member.getDailyImageRemaining()).isEqualTo(SubscriptionPolicy.PREMIUM_DAILY_IMAGE_LIMIT);
        assertThat(member.getLastTokenResetDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void 대상_회원이_없으면_아무_일도_안_일어난다() {
        // given
        when(memberRepository.findBySubscriptionPlanInAndSubscriptionEndAtBefore(
            eq(SubscriptionPolicy.PREMIUM_PLAN_TYPES), any(LocalDateTime.class)))
            .thenReturn(List.of());
        when(memberRepository.findBySubscriptionPlanInAndLastTokenResetDateNot(
            eq(SubscriptionPolicy.PREMIUM_PLAN_TYPES), any(LocalDate.class)))
            .thenReturn(List.of());

        // when
        subscriptionScheduler.runDailySubscriptionMaintenance();

        // then
        verify(subscriptionService, never()).reconcileIfExpired(any());
    }
}