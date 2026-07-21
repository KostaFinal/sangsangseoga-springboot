package com.kosta.sangsangseoga.domain.friendLibrary.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kosta.sangsangseoga.domain.friendLibrary.entity.Report;
import com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportStatus;
import com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportTargetType;
import com.kosta.sangsangseoga.domain.member.entity.Member;

public interface ReportRepository extends JpaRepository<Report, Long> {

	// 동일 회원이 동일 대상에 이미 신고했는지 확인 (중복 신고 방지)
	boolean existsByReporterAndTargetTypeAndTargetId(Member reporter, ReportTargetType targetType, Long targetId);

	// 내가 특정 targetType에 대해 신고한 대상 ID 목록 (신고 여부 재조회용)
	@Query("SELECT r.targetId FROM Report r WHERE r.reporter.id = :reporterId AND r.targetType = :targetType")
	List<Long> findTargetIdsByReporterIdAndTargetType(@Param("reporterId") Long reporterId,
			@Param("targetType") ReportTargetType targetType);

	// 관리자 신고 목록 조회 (상태별)
	Page<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status, Pageable pageable);

	// 내가 신고한 내역 조회
	Page<Report> findByReporter_IdOrderByCreatedAtDesc(Long reporterId, Pageable pageable);

	// 내가 신고한 내역 상태별 조회
	Page<Report> findByReporter_IdAndStatusOrderByCreatedAtDesc(Long reporterId, ReportStatus status,
			Pageable pageable);
	
	
	@Query(
		    value =
		        "SELECT r " +
		        "FROM Report r " +
		        "WHERE r.status = :status " +
		        "AND (" +
		        "   (" +
		        "       r.targetType = :bookType " +
		        "       AND EXISTS (" +
		        "           SELECT b.id " +
		        "           FROM Book b " +
		        "           WHERE b.id = r.targetId " +
		        "           AND b.member.id = :memberId" +
		        "       )" +
		        "   ) " +
		        "   OR " +
		        "   (" +
		        "       r.targetType = :commentType " +
		        "       AND EXISTS (" +
		        "           SELECT c.id " +
		        "           FROM Comment c " +
		        "           WHERE c.id = r.targetId " +
		        "           AND c.member.id = :memberId" +
		        "       )" +
		        "   )" +
		        ") " +
		        "ORDER BY r.processedAt DESC",
		    countQuery =
		        "SELECT COUNT(r) " +
		        "FROM Report r " +
		        "WHERE r.status = :status " +
		        "AND (" +
		        "   (" +
		        "       r.targetType = :bookType " +
		        "       AND EXISTS (" +
		        "           SELECT b.id " +
		        "           FROM Book b " +
		        "           WHERE b.id = r.targetId " +
		        "           AND b.member.id = :memberId" +
		        "       )" +
		        "   ) " +
		        "   OR " +
		        "   (" +
		        "       r.targetType = :commentType " +
		        "       AND EXISTS (" +
		        "           SELECT c.id " +
		        "           FROM Comment c " +
		        "           WHERE c.id = r.targetId " +
		        "           AND c.member.id = :memberId" +
		        "       )" +
		        "   )" +
		        ")"
		)
		Page<Report> findReceivedReports(
		        @Param("memberId") Long memberId,
		        @Param("status") ReportStatus status,
		        @Param("bookType") ReportTargetType bookType,
		        @Param("commentType") ReportTargetType commentType,
		        Pageable pageable
		);
	
	
}
