package com.kosta.sangsangseoga.domain.book.repository;
 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.kosta.sangsangseoga.domain.book.entity.WeeklyBookRanking;

import java.time.LocalDate;
import java.util.List;
 
public interface WeeklyBookRankingRepository extends JpaRepository<WeeklyBookRanking, Long> {
 
    // 이번 주 시작일 기준 상위 5개 조회 (score 내림차순)
    List<WeeklyBookRanking> findTop5ByWeekStartDateOrderByScoreDesc(LocalDate weekStartDate);
 
    // 가장 최근 집계 주 시작일 조회
    @Query("SELECT MAX(w.weekStartDate) FROM WeeklyBookRanking w")
    LocalDate findLatestWeekStartDate();
}