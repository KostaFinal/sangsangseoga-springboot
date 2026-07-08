package com.kosta.sangsangseoga.domain.admin.controller;

import com.kosta.sangsangseoga.domain.admin.dto.AdminReportListItemDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminReportListResponseDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminReportProcessRequestDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminReportProcessResponseDto;
import com.kosta.sangsangseoga.global.common.ApiResponse;
import com.kosta.sangsangseoga.global.config.ApiErrorCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Tag(name="Admin", description = "관리자 신고 처리 API (ADMIN 권한 필요)")
@RequestMapping("/api/admin")
public interface AdminApi {

    /**
     * GET /api/admin/reports
     * 아직 처리되지 않은(PENDING) 신고 목록 조회.
     */
    @Operation(summary = "미처리 신고 목록 조회", description = "PENDING 상태 신고를 최신순으로 페이지네이션 조회한다.")
    @ApiErrorCodes({}) // 인증(401) / 인가(403) 실패 외 도메인 에러 없음
    @GetMapping("/reports")
    ResponseEntity<ApiResponse<AdminReportListResponseDto>> getPendingReports(
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size);

    /**
     * PATCH /api/admin/reports/{reportId}
     * 신고 처리: 책 숨김/댓글 삭제/작가 정지/신고 기각.
     */
    @Operation(summary = "신고 처리", description = "actionType에 따라 책 숨김/ 댓글 삭제/ 작가 정지/ 신고 기각을 수행한다.")
    @ApiErrorCodes({"REPORT_NOT_FOUND", "REPORT_ALREADY_PROCESSED",
            "ACTION_TARGET_TYPE_MISMATCH", "ACTION_TARGET_NOT_FOUND", "MEMBER_NOT_FOUND"})
    @PatchMapping("/reports/{reportId}")
    public ResponseEntity<ApiResponse<AdminReportProcessResponseDto>> processReport(
        Authentication authentication,
        @PathVariable Long reportId,
        @Valid @RequestBody AdminReportProcessRequestDto request);
}
