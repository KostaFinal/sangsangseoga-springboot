package com.kosta.sangsangseoga.domain.subscription.scheduler;

import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
import com.kosta.sangsangseoga.domain.subscription.SubscriptionPolicy;
import com.kosta.sangsangseoga.domain.subscription.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    void 만료된_프리미엄_회원은_reconcileIfExpired가_회원별로_독립된_트랜잭션으로_호출된다() {
        // given
        Member expiredMember1 = Member.builder().email("aaa@test.com").password("aaa111").build();
        Member expiredMember2 = Member.builder().email("bbb@test.com").password("bbb222").build();
        ReflectionTestUtils.setField(expiredMember1, "id", 1L);
        ReflectionTestUtils.setField(expiredMember2, "id", 2L);
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

        // then: 회원 엔티티가 아니라 memberId로 위임해야 회원별 REQUIRES_NEW 트랜잭션 격리가 된다
        verify(subscriptionService).reconcileIfExpired(1L);
        verify(subscriptionService).reconcileIfExpired(2L);
    }

    @Test
    void 한_회원_처리_중_예외가_나도_나머지_회원_처리는_계속된다() {
        // given
        Member expiredMember1 = Member.builder().email("aaa@test.com").password("aaa111").build();
        Member expiredMember2 = Member.builder().email("bbb@test.com").password("bbb222").build();
        ReflectionTestUtils.setField(expiredMember1, "id", 1L);
        ReflectionTestUtils.setField(expiredMember2, "id", 2L);
        when(memberRepository.findBySubscriptionPlanInAndSubscriptionEndAtBefore(
            eq(SubscriptionPolicy.PREMIUM_PLAN_TYPES), any(LocalDateTime.class)))
            .thenReturn(List.of(expiredMember1, expiredMember2));
        when(memberRepository.findBySubscriptionPlanInAndLastTokenResetDateNot(
            eq(SubscriptionPolicy.PREMIUM_PLAN_TYPES), any(LocalDate.class)))
            .thenReturn(List.of());
        doThrow(new RuntimeException("DB 오류")).when(subscriptionService).reconcileIfExpired(1L);

        // when
        subscriptionScheduler.runDailySubscriptionMaintenance();

        // then: 1번 회원 처리가 예외를 던져도 2번 회원 처리는 그대로 호출된다
        verify(subscriptionService).reconcileIfExpired(2L);
    }

    @Test
    void 오늘_리셋_안된_프리미엄_회원은_resetDailyUsage가_회원별로_호출된다() {
        // given
        Member member = Member.builder().email("ccc@test.com").password("ccc333").build();
        ReflectionTestUtils.setField(member, "id", 3L);
        when(memberRepository.findBySubscriptionPlanInAndSubscriptionEndAtBefore(
            eq(SubscriptionPolicy.PREMIUM_PLAN_TYPES), any(LocalDateTime.class)))
            .thenReturn(List.of());
        when(memberRepository.findBySubscriptionPlanInAndLastTokenResetDateNot(
            eq(SubscriptionPolicy.PREMIUM_PLAN_TYPES), any(LocalDate.class)))
            .thenReturn(List.of(member));

        // when
        subscriptionScheduler.runDailySubscriptionMaintenance();

        // then: 실제 리셋 로직은 SubscriptionService 쪽에서 검증하고, 여기서는 위임 여부만 확인한다
        verify(subscriptionService).resetDailyUsage(3L);
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
        verifyNoInteractions(subscriptionService);
    }
}
