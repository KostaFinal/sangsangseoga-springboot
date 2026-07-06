package com.kosta.sangsangseoga.domain.subscription;

import com.kosta.sangsangseoga.domain.subscription.enums.PlanType;
import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;

import java.util.List;

/**
 * 플랜별 요금·한도 정책. DB에 플랜 테이블을 따로 두지 않고 정적 값으로 관리한다.
 * Gemini 2.5 Flash 단가(입력 $0.30/1M, 출력 $2.50/1M) 및 이미지 생성 단가(장당 약 $0.02~0.039)
 * 기준으로 월 요금 대비 AI 원가가 약 40% 선에 오도록 산정한 값이다. 연간 요금은 월 요금의
 * 10개월치(약 2개월 무료 혜택)로 산정했다.
 */
public final class SubscriptionPolicy {

    private SubscriptionPolicy() {
    }

    public static final int PREMIUM_MONTHLY_PRICE = 9_900;
    public static final int PREMIUM_MONTHLY_PERIOD_DAYS = 30;

    public static final int PREMIUM_YEARLY_PRICE = 99_000;
    public static final int PREMIUM_YEARLY_PERIOD_DAYS = 365;

    public static final int PREMIUM_DAILY_TEXT_LIMIT = 10;
    public static final int PREMIUM_DAILY_IMAGE_LIMIT = 3;

    /** FREE 회원의 생애 1회 무료 체험(책 1권 전체 생성)에 허용되는 최대 페이지 수. */
    public static final int FREE_TRIAL_PAGE_LIMIT = 10;

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
