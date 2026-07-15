package com.kosta.sangsangseoga.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminTokenTimelineItemDto {

    @Schema(description = "작업 시각(yyyy.MM.dd HH:mm)")
    private String date;

    @Schema(description = "작업 종류 설명")
    private String action;

    @Schema(description = "사용량 종류", allowableValues = {"text", "image"})
    private String usage;

    @Schema(description = "표시용 사용량 문자열. text면 \"1,840 자\", image면 \"4 장\" 형식")
    private String amount;
}
