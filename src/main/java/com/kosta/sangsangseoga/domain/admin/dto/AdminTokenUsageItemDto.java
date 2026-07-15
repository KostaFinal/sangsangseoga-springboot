package com.kosta.sangsangseoga.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminTokenUsageItemDto {

    @Schema(description = "회원 ID")
    private String userId;

    @Schema(description = "닉네임")
    private String nickname;

    @Schema(description = "구독 플랜", allowableValues = {"FREE", "PREMIUM"})
    private String plan;

    @Schema(description = "누적 텍스트 생성 사용량(Gemini 실제 토큰 수. FastAPI가 값을 안 준 옛 호출은 문자 수 근사치일 수 있음)")
    private Long textUsage;

    @Schema(description = "누적 이미지 생성 장수")
    private Long imgUsage;

    @Schema(description = "어뷰징 의심 여부. 판정 로직 미도입으로 항상 NORMAL 고정", allowableValues = {"NORMAL", "ABNORMAL"})
    private String status;
}
