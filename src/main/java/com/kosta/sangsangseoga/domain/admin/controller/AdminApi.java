package com.kosta.sangsangseoga.domain.admin.controller;

import com.kosta.sangsangseoga.domain.admin.dto.AdminActionLogListResponseDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminMemberListResponseDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminMemberStatusChangeRequestDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminMemberStatusChangeResponseDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminReportListResponseDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminReportProcessRequestDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminReportProcessResponseDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminTokenTimelineItemDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminTokenTrendItemDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminTokenUsageItemDto;
import com.kosta.sangsangseoga.domain.admin.enums.AdminActionType;
import com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportStatus;
import com.kosta.sangsangseoga.domain.member.enums.MemberStatus;
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
import javax.validation.constraints.Pattern;
import java.util.List;

@Tag(name="Admin", description = "관리자 신고 처리/회원 관리 API (ADMIN 권한 필요)")
@RequestMapping("/api/admin")
public interface AdminApi {

    /**
     * GET /api/admin/reports
     * 신고 목록 조회. status를 생략하면 PENDING(미처리) 신고만 조회한다.
     */
    @Operation(summary = "신고 목록 조회", description = "status(PENDING/RESOLVED/REJECTED) 상태 신고를 최신순으로 페이지네이션 조회한다. 생략 시 PENDING만 조회한다.")
    @ApiErrorCodes({}) // 인증(401) / 인가(403) 실패 외 도메인 에러 없음
    @GetMapping("/reports")
    ResponseEntity<ApiResponse<AdminReportListResponseDto>> getPendingReports(
        @RequestParam(required = false) ReportStatus status,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size);

    /**
     * PATCH /api/admin/reports/{reportId}
     * 신고 처리: 책 숨김/댓글 삭제/작가 정지/신고 기각.
     */
    @Operation(summary = "신고 처리", description = "actionType에 따라 책 숨김/ 댓글 삭제/ 신고 기각을 수행한다.")
    @ApiErrorCodes({"REPORT_NOT_FOUND", "REPORT_ALREADY_PROCESSED",
            "ACTION_TARGET_TYPE_MISMATCH", "ACTION_TARGET_NOT_FOUND", "MEMBER_NOT_FOUND",
            "ALREADY_DELETED_MEMBER"})
    @PatchMapping("/reports/{reportId}")
    public ResponseEntity<ApiResponse<AdminReportProcessResponseDto>> processReport(
        Authentication authentication,
        @PathVariable Long reportId,
        @Valid @RequestBody AdminReportProcessRequestDto request);

    /**
     * GET /api/admin/members
     * 전체 회원 목록 조회 (상태 필터 + 이메일/닉네임 검색어 + 페이지네이션).
     */
    @Operation(summary = "전체 회원 목록 조회", description = "상태 필터(ACTIVE/PENDING/SUSPENDED/DELETED)와 검색어로 전체 회원을 페이지네이션 조회한다.")
    @ApiErrorCodes({}) // 인증(401) / 인가(403) 실패 외 도메인 에러 없음
    @GetMapping("/members")
    ResponseEntity<ApiResponse<AdminMemberListResponseDto>> getMembers(
        @RequestParam(required = false) MemberStatus status,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size);

    /**
     * PATCH /api/admin/members/{memberId}/status
     * 회원 상태 강제 변경(정지/정상복원/탈퇴 처리).
     */
    @Operation(summary = "회원 상태 변경", description = "관리자가 회원을 정지(SUSPENDED)/정상복원(ACTIVE)/탈퇴(DELETED) 처리한다.")
    @ApiErrorCodes({"MEMBER_NOT_FOUND", "INVALID_TARGET_STATUS", "ALREADY_DELETED_MEMBER",
            "ADMIN_STATUS_CHANGE_NOT_ALLOWED"})
    @PatchMapping("/members/{memberId}/status")
    ResponseEntity<ApiResponse<AdminMemberStatusChangeResponseDto>> changeMemberStatus(
        Authentication authentication,
        @PathVariable Long memberId,
        @Valid @RequestBody AdminMemberStatusChangeRequestDto request);

    /**
     * GET /api/admin/action-logs
     * 관리자가 신고를 처리한 이력 조회.
     */
    @Operation(summary = "관리자 처리 이력 조회", description = "관리자가 신고를 처리한 이력(누가/언제/무슨 조치)을 최신순으로 페이지네이션 조회한다. "
            + "actionType을 생략하면 전체 조치 종류를 조회한다.")
    @ApiErrorCodes({}) // 인증(401) / 인가(403) 실패 외 도메인 에러 없음
    @GetMapping("/action-logs")
    ResponseEntity<ApiResponse<AdminActionLogListResponseDto>> getActionLogs(
        @RequestParam(required = false) AdminActionType actionType,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size);

    /**
     * GET /api/admin/token/trends
     * AI 사용량 트렌드(그래프용 시계열) 조회.
     */
    @Operation(summary = "AI 사용량 트렌드 조회", description = "unit=daily면 일별, unit=monthly면 월별 프리미엄/일반 회원 텍스트·이미지 사용량을 "
            + "시계열로 조회한다. 실사용 이력이 없는 구간도 0으로 채워서 반환한다.\n"
            + "- unit=daily: year+month를 함께 주면 그 달의 1일~말일 전체를 반환한다. 생략하면 오늘 기준 최근 7일.\n"
            + "- unit=monthly: year를 주면 그 해의 1월~12월을 반환한다(year와 months는 함께 쓰지 않으며, year가 우선). "
            + "year 없이 months만 주면 오늘 기준 최근 months개월. 둘 다 생략하면 최근 5개월.")
    @ApiErrorCodes({}) // 인증(401) / 인가(403) 실패 외 도메인 에러 없음
    @GetMapping("/token/trends")
    ResponseEntity<ApiResponse<List<AdminTokenTrendItemDto>>> getTokenTrends(
        @RequestParam(defaultValue = "daily") @Pattern(regexp = "daily|monthly") String unit,
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) @Min(1) @Max(12) Integer month,
        @RequestParam(required = false) @Min(1) @Max(60) Integer months);

    /**
     * GET /api/admin/token/usages
     * 회원별 AI 사용량 누적 랭킹 조회.
     */
    @Operation(summary = "회원별 AI 사용량 조회", description = "회원별 누적 텍스트/이미지 생성 사용량을 많은 순으로 조회한다. "
            + "어뷰징 판정 로직이 아직 없어 status는 항상 NORMAL로 내려온다.")
    @ApiErrorCodes({}) // 인증(401) / 인가(403) 실패 외 도메인 에러 없음
    @GetMapping("/token/usages")
    ResponseEntity<ApiResponse<List<AdminTokenUsageItemDto>>> getTokenUsages();

    /**
     * GET /api/admin/token/usages/{userId}/timeline
     * 특정 회원의 AI 작업 타임라인 조회.
     */
    @Operation(summary = "회원별 AI 작업 타임라인 조회", description = "특정 회원의 AI 생성 호출 이력을 최신순으로 조회한다.")
    @ApiErrorCodes({"MEMBER_NOT_FOUND"})
    @GetMapping("/token/usages/{userId}/timeline")
    ResponseEntity<ApiResponse<List<AdminTokenTimelineItemDto>>> getTokenUsageTimeline(
        @PathVariable Long userId);
}
