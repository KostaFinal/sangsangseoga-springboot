package com.kosta.sangsangseoga.domain.friendLibrary.repository;
 
import com.kosta.sangsangseoga.domain.account.entity.Member;
import com.kosta.sangsangseoga.domain.friendLibrary.entity.Report;
import com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
 
public interface ReportRepository extends JpaRepository<Report, Long> {
 
    // 동일 회원이 동일 대상에 이미 신고했는지 확인 (중복 신고 방지)
    boolean existsByReporterAndTargetTypeAndTargetId(Member reporter, ReportTargetType targetType, Long targetId);
}
 