package com.kosta.sangsangseoga.domain.member.repository;

import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.enums.AuthProvider;
import com.kosta.sangsangseoga.domain.member.enums.MemberStatus;
import com.kosta.sangsangseoga.domain.subscription.enums.PlanType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional<Member> findByAuthProviderAndOauthProviderId(AuthProvider authProvider, String oauthProviderId);

    List<Member> findBySubscriptionPlanInAndLastTokenResetDateNot(List<PlanType> subscriptionPlans, LocalDate lastTokenResetDate);

    List<Member> findBySubscriptionPlanInAndSubscriptionEndAtBefore(
            List<PlanType> subscriptionPlans, LocalDateTime subscriptionEndAt);

    // 관리자 회원 목록 조회: 상태 필터(null이면 전체)와 이메일/닉네임 검색어(null이면 전체)를 조합한다.
    @Query("SELECT m FROM Member m WHERE " +
            "(:status IS NULL OR m.status = :status) AND " +
            "(:keyword IS NULL OR m.email LIKE CONCAT('%', :keyword, '%') OR m.nickname LIKE CONCAT('%', :keyword, '%')) " +
            "ORDER BY m.createdAt DESC")
    Page<Member> searchForAdmin(@Param("status") MemberStatus status, @Param("keyword") String keyword, Pageable pageable);
}
