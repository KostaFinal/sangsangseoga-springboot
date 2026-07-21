package com.kosta.sangsangseoga.domain.ai.repository;

import com.kosta.sangsangseoga.domain.ai.entity.AiGenerationUsage;
import com.kosta.sangsangseoga.domain.ai.enums.CallType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AiGenerationUsageRepository extends JpaRepository<AiGenerationUsage, Long> {

    long countByMember_IdAndCallType(Long memberId, CallType callType);

    /**
     * 관리자 AI 사용량 트렌드 집계용. 회원의 플랜을 같이 봐야 해서 member를 fetch join한다.
     * to는 배타적 상한(exclusive)이라, 지나간 특정 월/연도를 조회할 때 그 이후 이력까지 불필요하게
     * 긁어오지 않는다.
     */
    @Query("SELECT a FROM AiGenerationUsage a JOIN FETCH a.member WHERE a.createdAt >= :from AND a.createdAt < :to")
    List<AiGenerationUsage> findAllWithMemberBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** 관리자 AI 사용량 랭킹 집계용. 전체 이력을 회원별로 그룹핑하기 위해 member를 fetch join한다. */
    @Query("SELECT a FROM AiGenerationUsage a JOIN FETCH a.member")
    List<AiGenerationUsage> findAllWithMember();

    /** 관리자 AI 사용량 타임라인 조회용. */
    List<AiGenerationUsage> findByMember_IdOrderByCreatedAtDesc(Long memberId);
}
