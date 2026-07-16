package com.kosta.sangsangseoga.domain.friendLibrary.repository;

import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.friendLibrary.entity.Report;
import com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportStatus;
import com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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
	// 내 책이 신고당한 내역
	@Query(
	    "SELECT r " +
	    "FROM Report r " +
	    "WHERE r.targetType = com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportTargetType.BOOK " +
	    "AND r.targetId IN (" +
	    "   SELECT b.id " +
	    "   FROM Book b " +
	    "   WHERE b.member.id = :memberId" +
	    ") " +
	    "ORDER BY r.createdAt DESC"
	)
	List<Report> findReceivedBookReports(
	        @Param("memberId") Long memberId
	);
	
	// 내가 작성한 댓글이 신고당한 내역
	@Query(
	    "SELECT r " +
	    "FROM Report r " +
	    "WHERE r.targetType = com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportTargetType.COMMENT " +
	    "AND r.targetId IN (" +
	    "   SELECT c.id " +
	    "   FROM Comment c " +
	    "   WHERE c.member.id = :memberId" +
	    ") " +
	    "ORDER BY r.createdAt DESC"
	)
	List<Report> findReceivedCommentReports(
	        @Param("memberId") Long memberId
	);
	
	
}
