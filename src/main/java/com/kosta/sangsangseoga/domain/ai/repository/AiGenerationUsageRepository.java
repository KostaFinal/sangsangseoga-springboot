package com.kosta.sangsangseoga.domain.ai.repository;

import com.kosta.sangsangseoga.domain.ai.entity.AiGenerationUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiGenerationUsageRepository extends JpaRepository<AiGenerationUsage, Long> {
}
