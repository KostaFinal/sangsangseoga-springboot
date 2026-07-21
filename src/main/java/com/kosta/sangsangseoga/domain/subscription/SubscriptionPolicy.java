package com.kosta.sangsangseoga.domain.subscription;

import com.kosta.sangsangseoga.domain.subscription.enums.PlanType;
import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;

import java.util.List;

/**
 * 플랜별 요금·한도 정책. DB 테이블 없이 정적 값으로 관리한다. Gemini/이미지 생성 원가 기준으로
 * 월 요금 대비 AI 원가가 약 40%가 되도록 산정했고, 연간 요금은 월 요금의 10개월치(2개월 무료)다.
 */
public final class SubscriptionPolicy {

    private SubscriptionPolicy() {
    }

    public static final int PREMIUM_MONTHLY_PRICE = 9_900;
    public static final int PREMIUM_MONTHLY_PERIOD_DAYS = 30;

    public static final int PREMIUM_YEARLY_PRICE = 99_000;
    public static final int PREMIUM_YEARLY_PERIOD_DAYS = 365;

    public static final int PREMIUM_DAILY_TEXT_LIMIT = 30;
    public static final int PREMIUM_DAILY_IMAGE_LIMIT = 20;

    /** FREE 회원의 생애 1회 무료 체험(책 1권 전체 생성)에 허용되는 최대 페이지 수. */
    public static final int FREE_TRIAL_PAGE_LIMIT = 10;

    /**
     * 체험판 페이지 수(10) 제한과는 별개로, 같은 페이지를 재생성으로 몇 번이고 다시 요청해
     * 원가만 계속 나가는 걸 막기 위한 생애 체험 전체 AI 호출 횟수 상한.
     */
    public static final int FREE_TRIAL_TEXT_CALL_LIMIT = 20;
    public static final int FREE_TRIAL_IMAGE_CALL_LIMIT = 15;

    public static final List<PlanType> PREMIUM_PLAN_TYPES = List.of(PlanType.PREMIUM_MONTHLY, PlanType.PREMIUM_YEARLY);

    public static int priceOf(PlanType planType) {
        switch (planType) {
            case PREMIUM_MONTHLY:
                return PREMIUM_MONTHLY_PRICE;
            case PREMIUM_YEARLY:
                return PREMIUM_YEARLY_PRICE;
            default:
                throw new CustomException(CommonErrorCode.BAD_REQUEST);
        }
    }

    public static int periodDaysOf(PlanType planType) {
        switch (planType) {
            case PREMIUM_MONTHLY:
                return PREMIUM_MONTHLY_PERIOD_DAYS;
            case PREMIUM_YEARLY:
                return PREMIUM_YEARLY_PERIOD_DAYS;
            default:
                throw new CustomException(CommonErrorCode.BAD_REQUEST);
        }
    }
}
