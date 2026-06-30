package com.kosta.sangsangseoga.domain.account.repository;

import com.kosta.sangsangseoga.domain.account.entity.GuardianConsent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuardianConsentRepository extends JpaRepository<GuardianConsent, Long> {
}
