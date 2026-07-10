package com.kosta.sangsangseoga.domain.admin.dto;

import com.kosta.sangsangseoga.domain.admin.enums.AdminActionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class AdminReportProcessRequestDto {

    @Schema(description = "처리 방식. targetType과 일치해야 한다(BOOK_HIDE<->BOOK, COMMENT_DELETE<->COMMENT, "
            + "AUTHOR_SUSPEND<->AUTHOR). REPORT_REJECT는 targetType과 무관하게 대상 조치 없이 신고만 기각한다.",
            requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"BOOK_HIDE", "COMMENT_DELETE", "AUTHOR_SUSPEND", "REPORT_REJECT"})
    @NotNull(message = "actionType은 필수입니다.")
    private AdminActionType actionType;

    @Schema(description = "처리 사유. AdminActionLog에 기록된다.", nullable = true)
    private String actionReason;
}
