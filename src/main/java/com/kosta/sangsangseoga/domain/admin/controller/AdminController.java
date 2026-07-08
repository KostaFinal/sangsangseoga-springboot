package com.kosta.sangsangseoga.domain.admin.controller;

import com.kosta.sangsangseoga.domain.admin.dto.AdminMemberListResponseDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminMemberStatusChangeRequestDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminMemberStatusChangeResponseDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminReportListResponseDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminReportProcessRequestDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminReportProcessResponseDto;
import com.kosta.sangsangseoga.domain.admin.service.AdminService;
import com.kosta.sangsangseoga.domain.member.enums.MemberStatus;
import com.kosta.sangsangseoga.global.common.ApiResponse;
import com.kosta.sangsangseoga.global.security.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 전용 엔드포인트. /api/admin/**는 SecurityConfig에서 hasRole("ADMIN")으로 막혀 있어
 * ADMIN 권한이 없는 로그인 회원은 403, 로그인 자체를 안 했으면 401로 이 컨트롤러에 도달하기 전에 걸러진다.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
public class AdminController implements AdminApi {

    private final AdminService adminService;

    @Override
    public ResponseEntity<ApiResponse<AdminReportListResponseDto>> getPendingReports(
            int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(adminService.getPendingReports(pageable)));
    }

    @Override
    public ResponseEntity<ApiResponse<AdminReportProcessResponseDto>> processReport(
            Authentication authentication, Long reportId,
            AdminReportProcessRequestDto request) {

        Long adminMemberId = AuthenticationHelper.resolveMemberId(authentication);
        AdminReportProcessResponseDto response = adminService.processReport(adminMemberId, reportId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Override
    public ResponseEntity<ApiResponse<AdminMemberListResponseDto>> getMembers(
            MemberStatus status, String keyword, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(adminService.getMembers(status, keyword, pageable)));
    }

    @Override
    public ResponseEntity<ApiResponse<AdminMemberStatusChangeResponseDto>> changeMemberStatus(
            Authentication authentication, Long memberId, AdminMemberStatusChangeRequestDto request) {

        Long adminMemberId = AuthenticationHelper.resolveMemberId(authentication);
        AdminMemberStatusChangeResponseDto response =
                adminService.changeMemberStatus(adminMemberId, memberId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
