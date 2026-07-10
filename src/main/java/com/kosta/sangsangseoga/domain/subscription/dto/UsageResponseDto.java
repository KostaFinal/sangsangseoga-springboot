package com.kosta.sangsangseoga.domain.subscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * plan=FREE면 아래 "FREE 전용" 필드만, plan=PREMIUM_MONTHLY/YEARLY면 "PREMIUM 전용" 필드만 채워진다.
 */
@Getter
@Builder
public class UsageResponseDto {

    @Schema(description = "현재 플랜", example = "FREE", allowableValues = {"FREE", "PREMIUM_MONTHLY", "PREMIUM_YEARLY"})
    private String plan;

    // PREMIUM 전용
    @Schema(description = "[PREMIUM 전용] 오늘 남은 텍스트 생성 횟수. FREE 회원은 null.", nullable = true)
    private Integer dailyTextRemaining;

    @Schema(description = "[PREMIUM 전용] 일일 텍스트 생성 한도. FREE 회원은 null.", nullable = true)
    private Integer dailyTextLimit;

    @Schema(description = "[PREMIUM 전용] 오늘 남은 이미지 생성 횟수. FREE 회원은 null.", nullable = true)
    private Integer dailyImageRemaining;

    @Schema(description = "[PREMIUM 전용] 일일 이미지 생성 한도. FREE 회원은 null.", nullable = true)
    private Integer dailyImageLimit;

    // FREE 전용
    @Schema(description = "[FREE 전용] 생애 1회 무료체험 사용 여부. PREMIUM 회원은 null.", nullable = true)
    private Boolean freeTrialUsed;

    @Schema(description = "[FREE 전용] 무료체험 최대 페이지 수. PREMIUM 회원은 null.", nullable = true)
    private Integer trialPageLimit;

    @Schema(description = "[FREE 전용] 무료체험 생애 텍스트 생성 호출 상한. PREMIUM 회원은 null.", nullable = true)
    private Integer freeTrialTextCallLimit;

    @Schema(description = "[FREE 전용] 무료체험 텍스트 생성 남은 호출 수. PREMIUM 회원은 null.", nullable = true)
    private Integer freeTrialTextCallsRemaining;

    @Schema(description = "[FREE 전용] 무료체험 생애 이미지 생성 호출 상한. PREMIUM 회원은 null.", nullable = true)
    private Integer freeTrialImageCallLimit;

    @Schema(description = "[FREE 전용] 무료체험 이미지 생성 남은 호출 수. PREMIUM 회원은 null.", nullable = true)
    private Integer freeTrialImageCallsRemaining;
}
