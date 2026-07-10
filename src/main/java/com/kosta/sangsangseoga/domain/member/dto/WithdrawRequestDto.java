package com.kosta.sangsangseoga.domain.member.dto;

import com.kosta.sangsangseoga.domain.member.enums.WithdrawalBookPolicy;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WithdrawRequestDto {

    @Schema(description = "본인 확인용 현재 비밀번호")
    private String password;

    @Schema(description = "내가 쓴 책 처리 방식. HIDE=비공개 전환(보존), DELETE=완전 삭제(복구 불가)")
    private WithdrawalBookPolicy bookPolicy;
}
