package com.kosta.sangsangseoga.domain.subscription.service;

import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
import com.kosta.sangsangseoga.domain.notification.service.NotificationService;
import com.kosta.sangsangseoga.domain.subscription.SubscriptionPolicy;
import com.kosta.sangsangseoga.domain.subscription.entity.Payment;
import com.kosta.sangsangseoga.domain.subscription.enums.PaymentStatus;
import com.kosta.sangsangseoga.domain.subscription.enums.PlanType;
import com.kosta.sangsangseoga.domain.subscription.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private SubscriptionService subscriptionService;

    // @InjectMocks는 @RequiredArgsConstructor가 만든 생성자(final 필드 3개)까지만 채우고, 생성자에
    // 안 잡히는 @PersistenceContext 필드(entityManager)는 채워주지 않아 수동으로 심어줘야 한다.
    @BeforeEach
    void setUpEntityManager() {
        ReflectionTestUtils.setField(subscriptionService, "entityManager", entityManager);
    }

    @Test
    void FREE_회원은_아무_처리도_하지_않는다() {
        // given
        Member member = Member.builder().email("a@test.com").password("pw").build();

        // when
        subscriptionService.reconcileIfExpired(member);

        // then
        verify(paymentRepository, never()).save(any());
        assertThat(member.getSubscriptionPlan()).isEqualTo(PlanType.FREE);
        verifyNoInteractions(notificationService);
    }

    @Test
    void 아직_만료되지_않은_프리미엄_회원은_아무_처리도_하지_않는다() {
        // given
        Member member = Member.builder().email("b@test.com").password("pw").build();
        member.startPremiumSubscription(PlanType.PREMIUM_MONTHLY,
            LocalDateTime.now(), LocalDateTime.now().plusDays(29), 10, 3);

        // when
        subscriptionService.reconcileIfExpired(member);

        // then
        verify(paymentRepository, never()).save(any());
        assertThat(member.getSubscriptionPlan()).isEqualTo(PlanType.PREMIUM_MONTHLY);
        verifyNoInteractions(notificationService);
    }

    @Test
    void 자동갱신_켜진_만료_회원은_재결제되고_기간이_연장된다() {
        // given
        Member member = Member.builder().email("c@test.com").password("pw").build();
        member.startPremiumSubscription(PlanType.PREMIUM_MONTHLY,
            LocalDateTime.now().minusDays(31), LocalDateTime.now().minusDays(1), 0, 0);
        // startPremiumSubscription이 autoRenew=true로 시작하므로 그대로 둔다 (해지 예약 안 한 상태)

        // when
        subscriptionService.reconcileIfExpired(member);

        // then
        // ArgumentCaptor : 메서드가 실행될 때 안의 내부 값들이 정확하게 세팅되어 들어갔는지. 상세하게 검증하고 싶을 때 사용
        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        // paymentRepository의 save() 메서드가 1회 호출되었는지 검증하면서,
        //    동시에 그때 전달된 Payment 객체를 captor로 '가로채서(capture)' 저장합니다.
        verify(paymentRepository).save(captor.capture());
        // Payment 객체를 꺼냅니다.
        Payment savedPayment = captor.getValue();
        assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(savedPayment.getPlanType()).isEqualTo(PlanType.PREMIUM_MONTHLY);
        assertThat(savedPayment.getAmount()).isEqualTo(SubscriptionPolicy.PREMIUM_MONTHLY_PRICE);

        assertThat(member.getSubscriptionPlan()).isEqualTo(PlanType.PREMIUM_MONTHLY);
        assertThat(member.getSubscriptionAutoRenew()).isTrue();
        assertThat(member.getSubscriptionEndAt()).isAfter(LocalDateTime.now());
        assertThat(member.getDailyTextRemaining()).isEqualTo(SubscriptionPolicy.PREMIUM_DAILY_TEXT_LIMIT);
        assertThat(member.getDailyImageRemaining()).isEqualTo(SubscriptionPolicy.PREMIUM_DAILY_IMAGE_LIMIT);
        verify(notificationService).notify(member, "구독이 자동 갱신되었습니다.");
    }

    @Test
    void 해지예약된_만료_회원은_FREE로_다운그레이드된다() {
        // given
        Member member = Member.builder().email("d@test.com").password("pw").build();
        member.startPremiumSubscription(PlanType.PREMIUM_MONTHLY,
            LocalDateTime.now().minusDays(31), LocalDateTime.now().minusDays(1), 5, 2);
        member.reserveCancellation(); // autoRenew=false로 전환 (해지 예약)

        // when
        subscriptionService.reconcileIfExpired(member);

        // then
        verify(paymentRepository, never()).save(any());
        assertThat(member.getSubscriptionPlan()).isEqualTo(PlanType.FREE);
        assertThat(member.getSubscriptionAutoRenew()).isFalse();
        assertThat(member.getDailyTextRemaining()).isZero();
        assertThat(member.getDailyImageRemaining()).isZero();
        verify(notificationService).notify(member, "구독 기간이 만료되어 FREE 요금제로 전환되었습니다.");
    }

    @Test
    void reconcileIfExpired_memberId_오버로드는_회원을_조회해서_동일한_정리_로직을_수행한다() {
        // given
        Member member = Member.builder().email("e@test.com").password("pw").build();
        ReflectionTestUtils.setField(member, "id", 10L);
        member.startPremiumSubscription(PlanType.PREMIUM_MONTHLY,
            LocalDateTime.now().minusDays(31), LocalDateTime.now().minusDays(1), 0, 0);
        member.reserveCancellation();
        when(memberRepository.findById(10L)).thenReturn(Optional.of(member));

        // when
        subscriptionService.reconcileIfExpired(10L);

        // then
        assertThat(member.getSubscriptionPlan()).isEqualTo(PlanType.FREE);
        verify(notificationService).notify(member, "구독 기간이 만료되어 FREE 요금제로 전환되었습니다.");
    }

    @Test
    void resetDailyUsage_memberId_오버로드는_회원의_일일_사용량을_재충전한다() {
        // given
        Member member = Member.builder().email("f@test.com").password("pw").build();
        ReflectionTestUtils.setField(member, "id", 11L);
        member.startPremiumSubscription(PlanType.PREMIUM_MONTHLY,
            LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(29), 0, 0);
        when(memberRepository.findById(11L)).thenReturn(Optional.of(member));

        // when
        subscriptionService.resetDailyUsage(11L);

        // then
        assertThat(member.getDailyTextRemaining()).isEqualTo(SubscriptionPolicy.PREMIUM_DAILY_TEXT_LIMIT);
        assertThat(member.getDailyImageRemaining()).isEqualTo(SubscriptionPolicy.PREMIUM_DAILY_IMAGE_LIMIT);
    }

    /*
     * 병렬 이미지 생성처럼 같은 회원에 대해 여러 요청이 동시에 reconcileIfExpired를 타는 상황을 재현한다.
     * 실제로는 두 트랜잭션이 동시에 만료된 member row를 읽고 각자 갱신을 시도하면, 먼저 커밋한 쪽만
     * 성공하고 나중 쪽은 Hibernate가 StaleStateException(-> ObjectOptimisticLockingFailureException)을
     * 던진다. 여기서는 그 "늦게 도착한 쪽"의 입장에서 saveAndFlush가 그 예외를 던지도록 흉내 낸다.
     */
    @Test
    void 동시_요청으로_낙관적_락_충돌이_나면_예외를_삼키고_최신값만_다시_읽는다_자동갱신() {
        // given
        Member member = Member.builder().email("g@test.com").password("pw").build();
        member.startPremiumSubscription(PlanType.PREMIUM_MONTHLY,
            LocalDateTime.now().minusDays(31), LocalDateTime.now().minusDays(1), 0, 0);
        when(memberRepository.saveAndFlush(member))
            .thenThrow(new ObjectOptimisticLockingFailureException(Member.class, member.getId()));

        // when: 먼저 도착한 다른 요청이 이미 처리해버린 상황(=saveAndFlush 실패)에서도 예외가 밖으로 새면 안 된다
        assertThatCode(() -> subscriptionService.reconcileIfExpired(member)).doesNotThrowAnyException();

        // then: 결제기록/알림은 "이긴 쪽"에서만 남아야 하므로, 충돌한 이 요청에서는 절대 만들면 안 된다
        verify(paymentRepository, never()).save(any());
        verifyNoInteractions(notificationService);
        // then: 낡은 값을 계속 들고 있지 않도록 DB에서 최신 상태를 다시 읽어와야 한다
        verify(entityManager).refresh(member);
    }

    @Test
    void 동시_요청으로_낙관적_락_충돌이_나면_예외를_삼키고_최신값만_다시_읽는다_해지예약() {
        // given
        Member member = Member.builder().email("h@test.com").password("pw").build();
        member.startPremiumSubscription(PlanType.PREMIUM_MONTHLY,
            LocalDateTime.now().minusDays(31), LocalDateTime.now().minusDays(1), 5, 2);
        member.reserveCancellation();
        when(memberRepository.saveAndFlush(member))
            .thenThrow(new ObjectOptimisticLockingFailureException(Member.class, member.getId()));

        // when
        assertThatCode(() -> subscriptionService.reconcileIfExpired(member)).doesNotThrowAnyException();

        // then
        verifyNoInteractions(notificationService);
        verify(entityManager).refresh(member);
    }
}