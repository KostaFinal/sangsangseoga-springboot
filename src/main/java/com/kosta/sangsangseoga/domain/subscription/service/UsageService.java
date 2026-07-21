package com.kosta.sangsangseoga.domain.subscription.service;

import com.kosta.sangsangseoga.domain.ai.enums.CallType;
import com.kosta.sangsangseoga.domain.ai.repository.AiGenerationUsageRepository;
import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
import com.kosta.sangsangseoga.domain.subscription.SubscriptionPolicy;
import com.kosta.sangsangseoga.domain.subscription.dto.UsageResponseDto;
import com.kosta.sangsangseoga.domain.subscription.exception.SubscriptionErrorCode;
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
    private final AiGenerationUsageRepository aiGenerationUsageRepository;

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
            long textCallsUsed = aiGenerationUsageRepository.countByMember_IdAndCallType(memberId, CallType.TEXT);
            long imageCallsUsed = aiGenerationUsageRepository.countByMember_IdAndCallType(memberId, CallType.IMAGE);

            builder.freeTrialUsed(Boolean.TRUE.equals(member.getFreeTrialUsed()))
                    .trialPageLimit(SubscriptionPolicy.FREE_TRIAL_PAGE_LIMIT)
                    .freeTrialTextCallLimit(SubscriptionPolicy.FREE_TRIAL_TEXT_CALL_LIMIT)
                    .freeTrialTextCallsRemaining(
                            (int) Math.max(0, SubscriptionPolicy.FREE_TRIAL_TEXT_CALL_LIMIT - textCallsUsed))
                    .freeTrialImageCallLimit(SubscriptionPolicy.FREE_TRIAL_IMAGE_CALL_LIMIT)
                    .freeTrialImageCallsRemaining(
                            (int) Math.max(0, SubscriptionPolicy.FREE_TRIAL_IMAGE_CALL_LIMIT - imageCallsUsed));
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

    /**
     * FREE 체험 동안 텍스트 생성을 더 호출할 수 있는지 확인한다. 페이지 수와 무관하게, 같은 페이지를
     * 계속 재생성하며 원가만 나가는 걸 막기 위한 생애 체험 전체 호출 횟수 상한이다.
     */
    public boolean canGenerateFreeTrialText(Long memberId) {
        return aiGenerationUsageRepository.countByMember_IdAndCallType(memberId, CallType.TEXT)
                < SubscriptionPolicy.FREE_TRIAL_TEXT_CALL_LIMIT;
    }

    /** FREE 체험 동안 이미지 생성을 더 호출할 수 있는지 확인한다. */
    public boolean canGenerateFreeTrialImage(Long memberId) {
        return aiGenerationUsageRepository.countByMember_IdAndCallType(memberId, CallType.IMAGE)
                < SubscriptionPolicy.FREE_TRIAL_IMAGE_CALL_LIMIT;
    }

    /**
     * PREMIUM 회원의 텍스트 생성 1회 차감. FREE 회원은 AiGenerationUsage insert(recordUsage) 자체가
     * 생애 호출 횟수 소진 처리라 여기서는 할 일이 없어 조용히 반환한다. 잔여량이 없으면 예외를 던진다.
     */
    public void consumeText(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));
        if (!member.getSubscriptionPlan().isPremium()) {
            return;
        }
        if (memberRepository.decrementDailyTextIfAvailable(memberId) == 0) {
            throw new CustomException(SubscriptionErrorCode.DAILY_QUOTA_EXCEEDED);
        }
    }

    /** PREMIUM 회원의 이미지 생성 1회 차감. {@link #consumeText} 참고. 잔여량이 없으면 예외를 던진다. */
    public void consumeImage(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));
        if (!member.getSubscriptionPlan().isPremium()) {
            return;
        }
        if (memberRepository.decrementDailyImageIfAvailable(memberId) == 0) {
            throw new CustomException(SubscriptionErrorCode.DAILY_QUOTA_EXCEEDED);
        }
    }

    /**
     * AI 텍스트 생성 전에 호출해 쿼터가 남아있는지만 확인한다(차감은 하지 않는다).
     * Python 호출은 비용이 드는 작업이라, 어차피 거절될 요청이면 호출 전에 걸러내기 위한 것이다.
     * 실제 차감은 Python 호출과 로컬 처리가 모두 끝난 뒤 {@link #consumeText}로 한다.
     */
    public void assertCanGenerateText(Long memberId) {
        assertCanGenerate(memberId, CallType.TEXT);
    }

    /** AI 이미지 생성 전에 호출해 쿼터가 남아있는지만 확인한다. {@link #assertCanGenerateText} 참고. */
    public void assertCanGenerateImage(Long memberId) {
        assertCanGenerate(memberId, CallType.IMAGE);
    }

    private void assertCanGenerate(Long memberId, CallType callType) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));
        subscriptionService.reconcileIfExpired(member);

        if (member.getSubscriptionPlan().isPremium()) {
            Integer remaining = callType == CallType.TEXT
                    ? member.getDailyTextRemaining()
                    : member.getDailyImageRemaining();
            if (remaining == null || remaining <= 0) {
                throw new CustomException(SubscriptionErrorCode.DAILY_QUOTA_EXCEEDED);
            }
        } else {
            boolean available = callType == CallType.TEXT
                    ? canGenerateFreeTrialText(memberId)
                    : canGenerateFreeTrialImage(memberId);
            if (!available) {
                throw new CustomException(SubscriptionErrorCode.FREE_TRIAL_CALL_LIMIT_EXCEEDED);
            }
        }
    }
}
