package com.kosta.sangsangseoga.domain.friendLibrary.controller;

import com.kosta.sangsangseoga.domain.friendLibrary.dto.ReportDto;
import com.kosta.sangsangseoga.domain.friendLibrary.dto.ReportRequestDto;
import com.kosta.sangsangseoga.domain.friendLibrary.service.ReportService;
import com.kosta.sangsangseoga.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
}