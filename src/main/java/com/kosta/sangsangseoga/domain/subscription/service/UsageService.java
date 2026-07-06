package com.kosta.sangsangseoga.domain.subscription.service;

import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
import com.kosta.sangsangseoga.domain.subscription.SubscriptionPolicy;
import com.kosta.sangsangseoga.domain.subscription.dto.UsageResponseDto;
import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UsageService {

    private final MemberRepository memberRepository;
    private final SubscriptionService subscriptionService;

    /** 조회만 하는 API지만 만료 정리에서 쓰기가 발생할 수 있어 readOnly로 두지 않는다. */
    public UsageResponseDto getUsage(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));
        subscriptionService.reconcileIfExpired(member);

        UsageResponseDto.UsageResponseDtoBuilder builder = UsageResponseDto.builder()
                .plan(member.getSubscriptionPlan().name());

        if (member.getSubscriptionPlan().isPremium()) {
            builder.dailyTextRemaining(member.getDailyTextRemaining())
                    .dailyTextLimit(SubscriptionPolicy.PREMIUM_DAILY_TEXT_LIMIT)
                    .dailyImageRemaining(member.getDailyImageRemaining())
                    .dailyImageLimit(SubscriptionPolicy.PREMIUM_DAILY_IMAGE_LIMIT);
        } else {
            builder.freeTrialUsed(Boolean.TRUE.equals(member.getFreeTrialUsed()))
                    .trialPageLimit(SubscriptionPolicy.FREE_TRIAL_PAGE_LIMIT);
        }

        return builder.build();
    }

    /**
     * FREE 회원이 새 책을 만들 수 있는지(=아직 생애 1회 체험을 안 썼는지) 확인한다.
     * 실제 호출 지점은 book 도메인의 "책 생성" API가 만들어질 때 붙는다.
     */
    public boolean canStartFreeTrial(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));
        subscriptionService.reconcileIfExpired(member);
        return !member.getSubscriptionPlan().isPremium()
                && !Boolean.TRUE.equals(member.getFreeTrialUsed());
    }

    /** FREE 체험 소진 처리. 새 책 생성이 시작되는 시점에 book 도메인에서 호출한다. */
    public void markFreeTrialUsed(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));
        member.useFreeTrial();
    }

    /** PREMIUM 회원의 텍스트 생성 1회 차감. 잔여량이 없으면 예외를 던진다. */
    public void consumeText(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));
        subscriptionService.reconcileIfExpired(member);
        if (member.getDailyTextRemaining() == null || member.getDailyTextRemaining() <= 0) {
            throw new CustomException(CommonErrorCode.BAD_REQUEST);
        }
        member.decrementDailyText();
    }

    /** PREMIUM 회원의 이미지 생성 1회 차감. 잔여량이 없으면 예외를 던진다. */
    public void consumeImage(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));
        subscriptionService.reconcileIfExpired(member);
        if (member.getDailyImageRemaining() == null || member.getDailyImageRemaining() <= 0) {
            throw new CustomException(CommonErrorCode.BAD_REQUEST);
        }
        member.decrementDailyImage();
    }
}
