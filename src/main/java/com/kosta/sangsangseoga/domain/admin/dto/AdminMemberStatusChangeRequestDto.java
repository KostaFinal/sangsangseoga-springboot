package com.kosta.sangsangseoga.domain.admin.dto;

import com.kosta.sangsangseoga.domain.member.enums.MemberStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class AdminMemberStatusChangeRequestDto {

    @NotNull(message = "status는 필수입니다.")
    private MemberStatus status; // ACTIVE(정상복원) / SUSPENDED(정지) / DELETED(탈퇴 처리)만 허용

    private String reason;
}
