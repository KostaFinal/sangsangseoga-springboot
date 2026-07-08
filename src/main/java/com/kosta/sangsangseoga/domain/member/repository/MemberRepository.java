package com.kosta.sangsangseoga.domain.member.repository;

import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.subscription.enums.PlanType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    List<Member> findBySubscriptionPlanInAndLastTokenResetDateNot(List<PlanType> subscriptionPlans, LocalDate lastTokenResetDate);

    List<Member> findBySubscriptionPlanInAndSubscriptionEndAtBefore(
            List<PlanType> subscriptionPlans, LocalDateTime subscriptionEndAt);
}
