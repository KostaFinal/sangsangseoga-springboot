package com.kosta.sangsangseoga.domain.admin.repository;

import com.kosta.sangsangseoga.domain.admin.entity.AdminActionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Long> {

    Page<AdminActionLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
