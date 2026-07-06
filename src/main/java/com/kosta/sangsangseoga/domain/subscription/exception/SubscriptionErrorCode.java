package com.kosta.sangsangseoga.domain.subscription.exception;

import com.kosta.sangsangseoga.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum SubscriptionErrorCode implements ErrorCode {

    ALREADY_PREMIUM_MEMBER(HttpStatus.CONFLICT, "이미 구독 중인 회원입니다. 해지 예약 상태라면 재개(resume) API를 이용해 주세요."),
    NOT_PREMIUM_MEMBER(HttpStatus.CONFLICT, "구독 중인 회원만 해지할 수 있습니다."),
    SUBSCRIPTION_ALREADY_CANCELLED(HttpStatus.CONFLICT, "이미 해지 예약된 구독입니다."),
    SUBSCRIPTION_NOT_CANCELLED(HttpStatus.CONFLICT, "해지 예약된 구독만 재개할 수 있습니다."),
    UNSUPPORTED_PLAN_TYPE(HttpStatus.BAD_REQUEST, "구매할 수 없는 플랜입니다.");

    private final HttpStatus status;
    private final String message;

    SubscriptionErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
