package com.kosta.sangsangseoga.domain.admin.controller;

import com.kosta.sangsangseoga.domain.admin.dto.AdminReportListResponseDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminReportProcessRequestDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminReportProcessResponseDto;
import com.kosta.sangsangseoga.domain.admin.service.AdminService;
import com.kosta.sangsangseoga.global.common.ApiResponse;
import com.kosta.sangsangseoga.global.security.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 관리자 전용 엔드포인트. /api/admin/**는 SecurityConfig에서 hasRole("ADMIN")으로 막혀 있어
 * ADMIN 권한이 없는 로그인 회원은 403, 로그인 자체를 안 했으면 401로 이 컨트롤러에 도달하기 전에 걸러진다.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
public class AdminController {

    private final AdminService adminService;

    /**
     * GET /api/admin/reports
     * 아직 처리되지 않은(PENDING) 신고 목록 조회.
     */
    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<AdminReportListResponseDto>> getPendingReports(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(adminService.getPendingReports(pageable)));
    }

    /**
     * PATCH /api/admin/reports/{reportId}
     * 신고 처리: 책 숨김/댓글 삭제/작가 정지/신고 기각.
     */
    @PatchMapping("/reports/{reportId}")
    public ResponseEntity<ApiResponse<AdminReportProcessResponseDto>> processReport(
            Authentication authentication,
            @PathVariable Long reportId,
            @Valid @RequestBody AdminReportProcessRequestDto request) {
        Long adminMemberId = AuthenticationHelper.resolveMemberId(authentication);
        AdminReportProcessResponseDto response = adminService.processReport(adminMemberId, reportId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
