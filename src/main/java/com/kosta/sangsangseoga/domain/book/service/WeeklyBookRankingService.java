package com.kosta.sangsangseoga.domain.book.service;

import com.kosta.sangsangseoga.domain.book.dto.WeeklyBookRankingDto;

public interface WeeklyBookRankingService {
 
    // 주간 인기 TOP5 조회
    WeeklyBookRankingDto getWeeklyRanking() throws Exception;
 
    // 이번 주 신작 TOP5 조회
    WeeklyBookRankingDto getWeeklyNewReleases() throws Exception;
}
