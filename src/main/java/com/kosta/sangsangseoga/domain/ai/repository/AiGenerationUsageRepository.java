package com.kosta.sangsangseoga.domain.ai.repository;

import com.kosta.sangsangseoga.domain.ai.entity.AiGenerationUsage;
import com.kosta.sangsangseoga.domain.ai.enums.CallType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiGenerationUsageRepository extends JpaRepository<AiGenerationUsage, Long> {

    long countByMember_IdAndCallType(Long memberId, CallType callType);
}
