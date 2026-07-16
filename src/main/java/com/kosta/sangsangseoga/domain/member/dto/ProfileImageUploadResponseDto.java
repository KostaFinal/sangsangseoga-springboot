package com.kosta.sangsangseoga.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProfileImageUploadResponseDto {

    @Schema(description = "업로드된 프로필 이미지 URL. 이 값을 PUT /api/members/me의 profileImageUrl로 저장한다.")
    private String profileImageUrl;
}
