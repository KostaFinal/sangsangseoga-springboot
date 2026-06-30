package com.kosta.sangsangseoga.domain.admin.repository;

import com.kosta.sangsangseoga.domain.admin.entity.WeeklyBookStat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyBookStatRepository extends JpaRepository<WeeklyBookStat, Long> {
}
