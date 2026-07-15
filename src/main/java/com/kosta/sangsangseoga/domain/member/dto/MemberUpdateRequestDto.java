package com.kosta.sangsangseoga.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;

@Getter
@NoArgsConstructor
public class MemberUpdateRequestDto {

    // null이면 검증을 건너뛰므로(부분 수정 방식과 호환) 생략된 필드는 그대로 유지된다.
    @Schema(description = "변경할 닉네임. 생략하거나 기존과 같으면 중복 검사를 하지 않는다.", nullable = true)
    @Pattern(regexp = "^[0-9A-Za-z가-힣]{2,10}$", message = "닉네임은 특수문자를 제외한 2~10자여야 합니다.")
    private String nickname;

    @Schema(description = "변경할 프로필 이미지 URL. POST /api/members/me/profile-image로 업로드한 URL을 그대로 넣는다.", nullable = true)
    private String profileImageUrl;

    @Schema(description = "변경할 소개글", nullable = true)
    private String introduction;
}
