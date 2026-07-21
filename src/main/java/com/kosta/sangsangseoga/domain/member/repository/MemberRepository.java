package com.kosta.sangsangseoga.domain.member.repository;

import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.enums.AuthProvider;
import com.kosta.sangsangseoga.domain.member.enums.MemberStatus;
import com.kosta.sangsangseoga.domain.subscription.enums.PlanType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    // PREMIUM 텍스트/이미지 생성 1회 원자적 차감. 여러 요청이 같은 회원의 잔여량을 동시에 깎을 때
    // (예: 시/에세이 완성 시 페이지 여러 개를 동시에 번역) load-modify-save 방식은 @Version 낙관적
    // 락 충돌(409)을 일으키므로, DB에 직접 UPDATE ... WHERE remaining > 0 한 번으로 처리한다.
    // 반환값이 0이면(조건에 안 맞아 갱신된 row가 없으면) 잔여량이 없다는 뜻이다.
    @Modifying
    @Query("UPDATE Member m SET m.dailyTextRemaining = m.dailyTextRemaining - 1 " +
            "WHERE m.id = :memberId AND m.dailyTextRemaining > 0")
    int decrementDailyTextIfAvailable(@Param("memberId") Long memberId);

    @Modifying
    @Query("UPDATE Member m SET m.dailyImageRemaining = m.dailyImageRemaining - 1 " +
            "WHERE m.id = :memberId AND m.dailyImageRemaining > 0")
    int decrementDailyImageIfAvailable(@Param("memberId") Long memberId);

    // 관리자 회원 목록 조회: 상태 필터(null이면 전체)와 이메일/닉네임 검색어(null이면 전체)를 조합한다.
    @Query("SELECT m FROM Member m WHERE " +
            "(:status IS NULL OR m.status = :status) AND " +
            "(:keyword IS NULL OR m.email LIKE CONCAT('%', :keyword, '%') OR m.nickname LIKE CONCAT('%', :keyword, '%')) " +
            "ORDER BY m.createdAt DESC")
    Page<Member> searchForAdmin(@Param("status") MemberStatus status, @Param("keyword") String keyword, Pageable pageable);
}
