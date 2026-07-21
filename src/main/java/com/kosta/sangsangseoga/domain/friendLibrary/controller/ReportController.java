package com.kosta.sangsangseoga.domain.friendLibrary.controller;

import com.kosta.sangsangseoga.domain.friendLibrary.dto.ReportDto;
import com.kosta.sangsangseoga.domain.friendLibrary.dto.ReportRequestDto;
import com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportTargetType;
import com.kosta.sangsangseoga.domain.friendLibrary.service.ReportService;
import com.kosta.sangsangseoga.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * POST /api/reports
     * 신고 등록 (책/댓글/작가 통합) - 201
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReportDto>> addReport(
            @RequestBody ReportRequestDto request,
            @AuthenticationPrincipal Long memberId) throws Exception {
        ReportDto result = reportService.addReport(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result));
    }

    /**
     * GET /api/reports/mine?targetType=BOOK|COMMENT|AUTHOR
     * 내가 신고한 대상 ID 목록 조회 - 200 (비로그인이면 빈 목록)
     */
    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<List<Long>>> getMyReportedTargetIds(
            @RequestParam ReportTargetType targetType,
            @AuthenticationPrincipal Long memberId) {
        List<Long> result = reportService.getMyReportedTargetIds(memberId, targetType);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}