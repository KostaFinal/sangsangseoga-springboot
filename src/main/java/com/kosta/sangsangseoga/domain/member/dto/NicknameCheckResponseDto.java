package com.kosta.sangsangseoga.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NicknameCheckResponseDto {

    @Schema(description = "닉네임 사용 가능 여부. true면 사용 가능, false면 이미 사용 중")
    private boolean available;
}
