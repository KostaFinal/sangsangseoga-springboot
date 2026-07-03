package com.kosta.sangsangseoga.domain.member.repository;

import com.kosta.sangsangseoga.domain.member.entity.GuardianConsent;
import com.kosta.sangsangseoga.domain.member.enums.GuardianConsentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuardianConsentRepository extends JpaRepository<GuardianConsent, Long> {

    boolean existsByMemberIdAndStatus(Long memberId, GuardianConsentStatus status);
}
