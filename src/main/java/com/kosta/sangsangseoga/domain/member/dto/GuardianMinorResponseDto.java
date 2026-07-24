package com.kosta.sangsangseoga.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class GuardianMinorResponseDto {

    @Schema(description = "미성년 회원 ID")
    private Long memberId;

    @Schema(description = "미성년 회원 닉네임")
    private String nickname;

    @Schema(description = "미성년 회원 이메일")
    private String email;

    @Schema(description = "미성년 회원 생년월일")
    private LocalDate birthDate;

    @Schema(description = "공개된(PUBLISHED) 작품 수")
    private Long bookCount;

    @Schema(description = "구독 플랜 표시용 라벨. 예: 무료, 프리미엄(월간), 프리미엄(연간)")
    private String subscriptionPlanLabel;
}
