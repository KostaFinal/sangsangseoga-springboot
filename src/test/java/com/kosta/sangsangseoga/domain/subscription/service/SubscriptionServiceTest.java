package com.kosta.sangsangseoga.domain.subscription.service;

import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
import com.kosta.sangsangseoga.domain.notification.service.NotificationService;
import com.kosta.sangsangseoga.domain.subscription.SubscriptionPolicy;
import com.kosta.sangsangseoga.domain.subscription.entity.Payment;
import com.kosta.sangsangseoga.domain.subscription.enums.PaymentStatus;
import com.kosta.sangsangseoga.domain.subscription.enums.PlanType;
import com.kosta.sangsangseoga.domain.subscription.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Test
    void FREE_회원은_아무_처리도_하지_않는다() {
        // given
        Member member = Member.builder().email("a@test.com").password("pw").build();

        // when
        subscriptionService.reconcileIfExpired(member);

        // then
        verify(paymentRepository, never()).save(any());
        assertThat(member.getSubscriptionPlan()).isEqualTo(PlanType.FREE);
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
    }
}