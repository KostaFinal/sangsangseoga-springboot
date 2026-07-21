package com.kosta.sangsangseoga.domain.member.dto;

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
}
