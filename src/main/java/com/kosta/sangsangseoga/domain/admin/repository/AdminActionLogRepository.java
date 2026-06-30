package com.kosta.sangsangseoga.domain.admin.repository;

import com.kosta.sangsangseoga.domain.admin.entity.AdminActionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Long> {
}
