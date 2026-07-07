package com.kosta.sangsangseoga.domain.admin.dto;

import com.kosta.sangsangseoga.domain.admin.enums.AdminActionType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class AdminReportProcessRequestDto {

    @NotNull(message = "actionType은 필수입니다.")
    private AdminActionType actionType;

    private String actionReason;
}
