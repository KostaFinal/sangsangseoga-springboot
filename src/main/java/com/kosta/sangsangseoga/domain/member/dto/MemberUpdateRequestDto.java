package com.kosta.sangsangseoga.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberUpdateRequestDto {

    @Schema(description = "변경할 닉네임. 생략하거나 기존과 같으면 중복 검사를 하지 않는다.", nullable = true)
    private String nickname;

    @Schema(description = "변경할 프로필 이미지 URL. POST /api/members/me/profile-image로 업로드한 URL을 그대로 넣는다.", nullable = true)
    private String profileImageUrl;

    @Schema(description = "변경할 소개글", nullable = true)
    private String introduction;
}
