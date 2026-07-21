package com.kosta.sangsangseoga.domain.admin.dto;

import com.kosta.sangsangseoga.domain.member.enums.MemberStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class AdminMemberStatusChangeRequestDto {

    @Schema(description = "변경할 상태. ACTIVE(정상복원)/SUSPENDED(정지)/DELETED(탈퇴 처리)만 허용된다. "
            + "PENDING은 회원가입/보호자 동의 흐름 전용이라 이 API로 지정할 수 없다.",
            requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"ACTIVE", "SUSPENDED", "DELETED"})
    @NotNull(message = "status는 필수입니다.")
    private MemberStatus status;

    @Schema(description = "처리 사유. 감사 로그에만 기록되고 DB에 영속되지 않는다.", nullable = true)
    private String reason;
}
