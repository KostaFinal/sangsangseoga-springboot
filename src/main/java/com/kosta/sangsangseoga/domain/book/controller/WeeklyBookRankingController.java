package com.kosta.sangsangseoga.domain.book.controller;


import com.kosta.sangsangseoga.domain.book.dto.WeeklyBookRankingDto;
import com.kosta.sangsangseoga.domain.book.service.WeeklyBookRankingService;
import com.kosta.sangsangseoga.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class WeeklyBookRankingController {

    private final WeeklyBookRankingService weeklyBookRankingService;

    /**
     * GET /api/books/weekly-ranking
     * 주간 인기 TOP5 조회 - 200 (비로그인 가능)
     */
    @GetMapping("/weekly-ranking")
    public ResponseEntity<ApiResponse<WeeklyBookRankingDto >> getWeeklyRanking() throws Exception {
        WeeklyBookRankingDto result = weeklyBookRankingService.getWeeklyRanking();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * GET /api/books/weekly-new-releases
     * 이번 주 신작 TOP5 조회 - 200 (비로그인 가능)
     */
    @GetMapping("/weekly-new-releases")
    public ResponseEntity<ApiResponse<WeeklyBookRankingDto>> getWeeklyNewReleases() throws Exception {
        WeeklyBookRankingDto result = weeklyBookRankingService.getWeeklyNewReleases();
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}