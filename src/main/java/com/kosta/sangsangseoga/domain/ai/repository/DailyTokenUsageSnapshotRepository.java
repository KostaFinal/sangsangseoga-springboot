package com.kosta.sangsangseoga.domain.ai.repository;

import com.kosta.sangsangseoga.domain.ai.entity.DailyTokenUsageSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyTokenUsageSnapshotRepository extends JpaRepository<DailyTokenUsageSnapshot, Long> {
}
