package com.kosta.sangsangseoga.domain.admin.repository;

import com.kosta.sangsangseoga.domain.admin.entity.AdminActionLog;
import com.kosta.sangsangseoga.domain.admin.enums.AdminActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Long> {

    Page<AdminActionLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<AdminActionLog> findByActionTypeOrderByCreatedAtDesc(AdminActionType actionType, Pageable pageable);

    /**
     * 신고 목록 응답에 처리 결과(사유/처리자 닉네임)를 같이 내려주기 위한 조회.
     * admin을 fetch join해 항목 수만큼 N+1이 나지 않게 한다.
     */
    @Query("SELECT a FROM AdminActionLog a JOIN FETCH a.admin WHERE a.report.id IN :reportIds")
    List<AdminActionLog> findByReportIdInWithAdmin(@Param("reportIds") List<Long> reportIds);
}
