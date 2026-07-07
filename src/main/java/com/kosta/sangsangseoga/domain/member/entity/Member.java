package com.kosta.sangsangseoga.domain.member.entity;

import com.kosta.sangsangseoga.domain.member.enums.MemberRole;
import com.kosta.sangsangseoga.domain.member.enums.MemberStatus;
import com.kosta.sangsangseoga.domain.member.enums.ViewerFontSize;
import com.kosta.sangsangseoga.domain.member.enums.ViewerViewType;
import com.kosta.sangsangseoga.domain.subscription.enums.PlanType;
import com.kosta.sangsangseoga.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private LocalDate birthDate;

    @Column(unique = true)
    private String nickname;

    private String profileImageUrl;

    @Lob
    private String introduction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @Column(nullable = false)
    private Boolean freeTrialUsed;

    @Enumerated(EnumType.STRING)
    private ViewerFontSize viewerFontSize;

    @Enumerated(EnumType.STRING)
    private ViewerViewType viewerViewType;

    private LocalDateTime withdrawnAt;

    @Enumerated(EnumType.STRING)
    private PlanType subscriptionPlan;

    private LocalDateTime subscriptionStartAt;

    private LocalDateTime subscriptionEndAt;

    private Integer dailyTextRemaining;

    private Integer dailyImageRemaining;

    private LocalDate lastTokenResetDate;

    @Builder
    private Member(String email, String password, LocalDate birthDate, String nickname,
                    String profileImageUrl, String introduction, MemberStatus status, MemberRole role,
                    Boolean freeTrialUsed, ViewerFontSize viewerFontSize, ViewerViewType viewerViewType,
                    PlanType subscriptionPlan) {
        this.email = email;
        this.password = password;
        this.birthDate = birthDate;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.introduction = introduction;
        this.status = status != null ? status : MemberStatus.ACTIVE;
        this.role = role != null ? role : MemberRole.USER;
        this.freeTrialUsed = freeTrialUsed != null ? freeTrialUsed : false;
        this.viewerFontSize = viewerFontSize;
        this.viewerViewType = viewerViewType;
        this.subscriptionPlan = subscriptionPlan != null ? subscriptionPlan : PlanType.FREE;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void activate() {
        this.status = MemberStatus.ACTIVE;
    }

    /**
     * 보호자가 승인을 철회했을 때, 재동의 전까지 이용을 다시 막기 위해 가입 대기 상태로 되돌린다.
     */
    public void revertToPending() {
        this.status = MemberStatus.PENDING;
    }

    public void withdraw() {
        this.status = MemberStatus.DELETED;
        this.withdrawnAt = LocalDateTime.now();
    }

    /**
     * 탈퇴에 따른 구독 즉시 해지. 환불/잔여 기간 보상 없이 즉시 종료한다.
     * ERD에 해지 사유 컬럼이 없어 사유는 별도로 남기지 않는다.
     */
    public void cancelSubscriptionImmediately() {
        this.subscriptionPlan = PlanType.FREE;
        this.subscriptionEndAt = LocalDateTime.now();
    }
}
