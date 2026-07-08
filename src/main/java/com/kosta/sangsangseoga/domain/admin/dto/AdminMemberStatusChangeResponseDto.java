package com.kosta.sangsangseoga.domain.admin.dto;

import com.kosta.sangsangseoga.domain.member.enums.MemberStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminMemberStatusChangeResponseDto {

    private Long memberId;
    private MemberStatus status;
    private LocalDateTime processedAt;
}
