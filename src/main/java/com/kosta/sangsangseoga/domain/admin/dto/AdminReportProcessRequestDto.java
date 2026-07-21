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

	@Schema(
		    description =
		        "처리 방식. BOOK_HIDE는 BOOK, COMMENT_DELETE는 COMMENT 신고에만 사용할 수 있다. "
		        + "REPORT_REJECT는 대상 조치 없이 신고를 기각한다.",
		    requiredMode = Schema.RequiredMode.REQUIRED,
		    allowableValues = {
		        "BOOK_HIDE",
		        "COMMENT_DELETE",
		        "REPORT_REJECT"
		    }
		)
    @NotNull(message = "actionType은 필수입니다.")
    private AdminActionType actionType;

    @Schema(description = "처리 사유. AdminActionLog에 기록된다.", nullable = true)
    private String actionReason;
}
