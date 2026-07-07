package com.kosta.sangsangseoga.domain.subscription.controller;

import com.kosta.sangsangseoga.domain.subscription.dto.SubscriptionCreateRequestDto;
import com.kosta.sangsangseoga.domain.subscription.dto.SubscriptionMeResponseDto;
import com.kosta.sangsangseoga.domain.subscription.dto.SubscriptionPlanDto;
import com.kosta.sangsangseoga.domain.subscription.service.SubscriptionService;
import com.kosta.sangsangseoga.global.common.ApiResponse;
import com.kosta.sangsangseoga.global.security.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SubscriptionController {
    // GET /api/subscription-plans, GET·POST /api/subscriptions, POST /api/subscriptions/cancel
    // 서로 다른 최상위 경로라 클래스 레벨 매핑 없이 메서드별로 전체 경로를 지정합니다.

    private final SubscriptionService subscriptionService;

    @GetMapping("/api/subscription-plans")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanDto>>> getPlans() {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getPlans()));
    }

    @GetMapping("/api/subscriptions/me")
    public ResponseEntity<ApiResponse<SubscriptionMeResponseDto>> getMySubscription(Authentication authentication) {
        Long memberId = AuthenticationHelper.resolveMemberId(authentication);
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getMySubscription(memberId)));
    }

    /**
     * 정기구독 시작. 프론트가 PG 결제위젯(Mock)을 완료한 뒤 paymentKey/orderId/amount를 담아 호출하는
     * 단일 콜백 엔드포인트다. 별도의 "주문 생성 -> 승인" 2단계가 아니라 이 호출 한 번으로 구독이 반영된다.
     */
    @PostMapping("/api/subscriptions")
    public ResponseEntity<ApiResponse<SubscriptionMeResponseDto>> subscribe(
            Authentication authentication,
            @RequestBody SubscriptionCreateRequestDto request) {
        Long memberId = AuthenticationHelper.resolveMemberId(authentication);
        SubscriptionMeResponseDto response = subscriptionService.subscribe(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 월간 -> 연간 즉시 전환(재결제, 남은 월간 기간 소멸). 연간 -> 월간 다운그레이드는 지원하지 않는다
     * (해지 예약 후 만료를 기다렸다가 월간으로 재구독하는 기존 흐름을 이용해야 함).
     */
    @PatchMapping("/api/subscriptions")
    public ResponseEntity<ApiResponse<SubscriptionMeResponseDto>> changePlan(
            Authentication authentication,
            @RequestBody SubscriptionCreateRequestDto request) {
        Long memberId = AuthenticationHelper.resolveMemberId(authentication);
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.changePlan(memberId, request)));
    }

    @PostMapping("/api/subscriptions/cancel")
    public ResponseEntity<ApiResponse<SubscriptionMeResponseDto>> cancelSubscription(Authentication authentication) {
        Long memberId = AuthenticationHelper.resolveMemberId(authentication);
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.cancelSubscription(memberId)));
    }

    /**
     * 해지 예약 취소(재개). 아직 혜택 기간이 남아있을 때 재결제 없이 자동갱신만 다시 켠다.
     */
    @PostMapping("/api/subscriptions/resume")
    public ResponseEntity<ApiResponse<SubscriptionMeResponseDto>> resumeSubscription(Authentication authentication) {
        Long memberId = AuthenticationHelper.resolveMemberId(authentication);
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.resumeSubscription(memberId)));
    }
}
