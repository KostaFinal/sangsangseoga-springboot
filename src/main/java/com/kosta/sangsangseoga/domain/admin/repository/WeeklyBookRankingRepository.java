package com.kosta.sangsangseoga.domain.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kosta.sangsangseoga.domain.book.entity.WeeklyBookRanking;

public interface WeeklyBookRankingRepository extends JpaRepository<WeeklyBookRanking, Long> {
}
