package com.kosta.sangsangseoga.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminTokenTrendItemDto {

    @Schema(description = "구간 라벨. unit=daily면 MM/DD, unit=monthly면 N월 형식")
    private String label;

    @Schema(description = "프리미엄 회원 텍스트 생성 사용량(만 토큰 단위, 소수 첫째 자리까지)")
    private Double premiumTxt;

    @Schema(description = "일반(FREE) 회원 텍스트 생성 사용량(만 토큰 단위, 소수 첫째 자리까지)")
    private Double freeTxt;

    @Schema(description = "프리미엄 회원 이미지 생성 장수")
    private Integer premiumImg;

    @Schema(description = "일반(FREE) 회원 이미지 생성 장수")
    private Integer freeImg;
}
