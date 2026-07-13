package com.kosta.sangsangseoga.domain.myLibrary.controller;

import java.time.LocalDate;
import java.util.List;

import javax.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kosta.sangsangseoga.domain.myLibrary.dto.ReadingPlanRequestDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.ReadingPlanResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.service.ReadingPlanService;
import com.kosta.sangsangseoga.global.common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bookshelves/reading-plans")
@RequiredArgsConstructor
public class ReadingPlanController {
	private final ReadingPlanService readingPlanService;

    /**
     * 전체 독서 계획 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReadingPlanResponseDto>>> getReadingPlans(
            Authentication authentication) {

        Long memberId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                ApiResponse.success(
                        readingPlanService.getReadingPlans(memberId)
                )
        );
    }

    /**
     * 특정 날짜 독서 계획 조회
     */
    @GetMapping("/date")
    public ResponseEntity<ApiResponse<List<ReadingPlanResponseDto>>> getReadingPlansByDate(
            Authentication authentication,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate planDate) {

        Long memberId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                ApiResponse.success(
                        readingPlanService.getReadingPlansByDate(memberId, planDate)
                )
        );
    }

    /**
     * 독서 계획 등록
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReadingPlanResponseDto>> createReadingPlan(
            Authentication authentication,
            @Validated(ReadingPlanRequestDto.OnCreate.class)
            @RequestBody ReadingPlanRequestDto requestDto) {

        Long memberId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                ApiResponse.success(
                        readingPlanService.createReadingPlan(memberId, requestDto)
                )
        );
    }

    /**
     * 독서 계획 수정
     */
    @PatchMapping("/{planId}")
    public ResponseEntity<ApiResponse<ReadingPlanResponseDto>> updateReadingPlan(
            Authentication authentication,
            @PathVariable Long planId,
            @Valid @RequestBody ReadingPlanRequestDto requestDto) {


        Long memberId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                ApiResponse.success(
                        readingPlanService.updateReadingPlan(memberId, planId, requestDto)
                )
        );
    }

    /**
     * 독서 계획 삭제
     */
    @DeleteMapping("/{planId}")
    public ResponseEntity<ApiResponse<Void>> deleteReadingPlan(
            Authentication authentication,
            @PathVariable Long planId) {

        Long memberId = (Long) authentication.getPrincipal();

        readingPlanService.deleteReadingPlan(memberId, planId);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 독서 계획 완료
     */
    @PatchMapping("/{planId}/complete")
    public ResponseEntity<ApiResponse<ReadingPlanResponseDto>> completeReadingPlan(
            Authentication authentication,
            @PathVariable Long planId) {

        Long memberId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                ApiResponse.success(
                        readingPlanService.completeReadingPlan(memberId, planId)
                )
        );
    }
}
