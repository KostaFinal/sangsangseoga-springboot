package com.kosta.sangsangseoga.domain.subscription.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SubscriptionController {
    // GET /api/subscription-plans, GET·DELETE /api/subscriptions/me
    // 서로 다른 최상위 경로라 클래스 레벨 매핑 없이 메서드별로 전체 경로를 지정합니다.
}
