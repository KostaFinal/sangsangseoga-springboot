package com.kosta.sangsangseoga.domain.admin.dto;

import com.kosta.sangsangseoga.domain.member.enums.MemberStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminMemberStatusChangeResponseDto {

    @Schema(description = "회원 ID")
    private Long memberId;

    @Schema(description = "변경 후 상태")
    private MemberStatus status;

    @Schema(description = "처리 시각")
    private LocalDateTime processedAt;
}
