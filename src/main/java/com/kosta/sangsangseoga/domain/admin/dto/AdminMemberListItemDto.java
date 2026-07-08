package com.kosta.sangsangseoga.domain.admin.dto;

import com.kosta.sangsangseoga.domain.member.enums.MemberRole;
import com.kosta.sangsangseoga.domain.member.enums.MemberStatus;
import com.kosta.sangsangseoga.domain.subscription.enums.PlanType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminMemberListItemDto {

    private Long memberId;
    private String email;
    private String nickname;
    private MemberStatus status;
    private MemberRole role;
    private PlanType subscriptionPlan;
    private LocalDateTime createdAt;
    private LocalDateTime withdrawnAt;
}
