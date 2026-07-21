package com.kosta.sangsangseoga.domain.admin.dto;

import com.kosta.sangsangseoga.domain.member.enums.MemberRole;
import com.kosta.sangsangseoga.domain.member.enums.MemberStatus;
import com.kosta.sangsangseoga.domain.subscription.enums.PlanType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminMemberListItemDto {

    @Schema(description = "회원 ID")
    private Long memberId;

    @Schema(description = "이메일")
    private String email;

    @Schema(description = "닉네임")
    private String nickname;

    @Schema(description = "회원 상태")
    private MemberStatus status;

    @Schema(description = "권한")
    private MemberRole role;

    @Schema(description = "구독 플랜")
    private PlanType subscriptionPlan;

    @Schema(description = "가입일시")
    private LocalDateTime createdAt;

    @Schema(description = "탈퇴일시. 탈퇴하지 않았으면 null.", nullable = true)
    private LocalDateTime withdrawnAt;
}
