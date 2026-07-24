package com.kosta.sangsangseoga.domain.member.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.annotations.OptimisticLock;

import com.kosta.sangsangseoga.domain.member.enums.AuthProvider;
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
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member", uniqueConstraints = {
        @UniqueConstraint(name = "uk_member_auth_provider", columnNames = {"auth_provider", "oauth_provider_id"})
})
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 낙관적 락. 구독/상태 변경처럼 같은 회원 row를 동시에 읽고 쓸 수 있는 흐름에서 나중에 커밋되는
     * 쪽만 ObjectOptimisticLockingFailureException으로 막는다. 프로필/뷰어 설정 등 동시 수정 충돌에
     * 안전한 필드는 @OptimisticLock(excluded = true)로 제외했다.
     */
    @Version
    private Long version;

    @OptimisticLock(excluded = true)
    @Column(nullable = false, unique = true)
    private String email;

    @OptimisticLock(excluded = true)
    @Column(nullable = false)
    private String password;

    @OptimisticLock(excluded = true)
    private LocalDate birthDate;

    @OptimisticLock(excluded = true)
    @Column(unique = true)
    private String nickname;

    @OptimisticLock(excluded = true)
    @Lob
    private String profileImageUrl;

    @OptimisticLock(excluded = true)
    @Lob
    private String introduction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    @OptimisticLock(excluded = true)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @Column(nullable = false)
    private Boolean freeTrialUsed;

    /**
     * FREE 상태로 무료체험 AI 생성(텍스트/이미지)을 한 번이라도 시작했는지. book.publish() 시점에 이미
     * PREMIUM으로 바뀌어 있어도, 이 책이 FREE 체험으로 시작됐다면 무료체험 슬롯을 소진 처리하기 위한
     * 표식이다({@link com.kosta.sangsangseoga.domain.book.service.BookServiceImpl#publish} 참고).
     */
    private Boolean freeTrialStarted;

    /** 로컬(이메일/비밀번호) 가입이면 LOCAL, 소셜 로그인으로 가입했으면 KAKAO/NAVER. */
    @OptimisticLock(excluded = true)
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false)
    private AuthProvider authProvider;

    /** 소셜 제공자가 내려주는 회원 식별자. LOCAL 계정은 NULL. (authProvider, oauthProviderId) 조합으로 유니크. */
    @OptimisticLock(excluded = true)
    @Column(name = "oauth_provider_id")
    private String oauthProviderId;

    @OptimisticLock(excluded = true)
    @Enumerated(EnumType.STRING)
    private ViewerFontSize viewerFontSize;

    @OptimisticLock(excluded = true)
    @Enumerated(EnumType.STRING)
    private ViewerViewType viewerViewType;

    private LocalDateTime withdrawnAt;

    @Enumerated(EnumType.STRING)
    private PlanType subscriptionPlan;

    private LocalDateTime subscriptionStartAt;

    private LocalDateTime subscriptionEndAt;

    /** true면 결제 주기 만료 시 배치가 자동으로 재결제(Mock)를 시뮬레이션해 이어서 PREMIUM 유지. */
    private Boolean subscriptionAutoRenew;

    private Integer dailyTextRemaining;

    private Integer dailyImageRemaining;

    private LocalDate lastTokenResetDate;

    @Builder
    private Member(String email, String password, LocalDate birthDate, String nickname,
                    String profileImageUrl, String introduction, MemberStatus status, MemberRole role,
                    Boolean freeTrialUsed, ViewerFontSize viewerFontSize, ViewerViewType viewerViewType,
                    PlanType subscriptionPlan, AuthProvider authProvider, String oauthProviderId) {
        this.email = email;
        this.password = password;
        this.birthDate = birthDate;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.introduction = introduction;
        this.status = status != null ? status : MemberStatus.ACTIVE;
        this.role = role != null ? role : MemberRole.USER;
        this.freeTrialUsed = freeTrialUsed != null ? freeTrialUsed : false;
        this.viewerFontSize = viewerFontSize != null ? viewerFontSize : ViewerFontSize.MEDIUM;
        this.viewerViewType = viewerViewType != null ? viewerViewType : ViewerViewType.FLIP;
        this.subscriptionPlan = subscriptionPlan != null ? subscriptionPlan : PlanType.FREE;
        this.authProvider = authProvider != null ? authProvider : AuthProvider.LOCAL;
        this.oauthProviderId = oauthProviderId;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void activate() {
        this.status = MemberStatus.ACTIVE;
    }

    /**
     * 관리자가 신고를 처리하며(AUTHOR_SUSPEND) 작가 계정을 이용정지 시킬 때 호출한다.
     * 로그인 시 SUSPENDED 상태는 이미 차단되고 있다(AuthService.login).
     */
    public void suspend() {
        this.status = MemberStatus.SUSPENDED;
    }

    /**
     * 보호자가 승인을 철회했을 때, 재동의 전까지 이용을 다시 막기 위해 가입 대기 상태로 되돌린다.
     */
    public void revertToPending() {
        this.status = MemberStatus.PENDING;
    }

    /**
     * 관리자가 신고 처리 등으로 강제 탈퇴시킬 때 호출한다. email/nickname/oauthProviderId를
     * 그대로 보존해 같은 정보로 재가입할 수 없게 막는다(제재 우회 방지) - 관리자 탈퇴는 영구 차단으로
     * 취급한다({@link com.kosta.sangsangseoga.domain.admin.service.AdminService#changeMemberStatus} 참고).
     * 본인이 직접 탈퇴하는 경우는 {@link #withdrawAndReleaseIdentifiers()}를 대신 쓴다.
     */
    public void withdraw() {
        this.status = MemberStatus.DELETED;
        this.withdrawnAt = LocalDateTime.now();
    }

    /**
     * 본인 탈퇴 전용. 관리자 강제 탈퇴와 달리 제재 우회를 걱정할 필요가 없으므로, email/nickname/
     * oauthProviderId를 풀어(mangle) 같은 이메일/소셜 계정으로 재가입하면 기존 이력과 무관한 완전히
     * 새 계정으로 시작할 수 있게 한다({@link com.kosta.sangsangseoga.domain.member.service.MemberService#withdraw} 참고).
     */
    public void withdrawAndReleaseIdentifiers() {
        withdraw();
        this.email = "withdrawn-" + this.id + "-" + this.email;
        this.nickname = null;
        this.oauthProviderId = null;
    }

    /**
     * 보호자 동의 거절로 최초 가입 게이트를 통과하지 못해 가입 자체가 취소될 때 호출한다. 일반 탈퇴
     * (withdraw)와 달리 email/nickname/oauthProviderId에 실제 DB unique 제약이 걸려 있어서 상태만
     * DELETED로 바꾸는 것으로는 같은 정보로 재가입할 수 없다. 로그인을 한 번도 못 해본 미완성 가입이라
     * 원본 식별자를 보존할 실익이 없으므로, 값 자체를 풀어(mangle) 재가입이 가능하게 한다.
     */
    public void cancelPendingSignup() {
        this.status = MemberStatus.DELETED;
        this.withdrawnAt = LocalDateTime.now();
        this.email = "cancelled-" + this.id + "-" + this.email;
        this.nickname = null;
        this.oauthProviderId = null;
    }

    /**
     * 탈퇴에 따른 구독 즉시 해지. 환불/잔여 기간 보상 없이 즉시 종료한다.
     * ERD에 해지 사유 컬럼이 없어 사유는 별도로 남기지 않는다.
     */
    public void cancelSubscriptionImmediately() {
        this.subscriptionPlan = PlanType.FREE;
        this.subscriptionEndAt = LocalDateTime.now();
        this.subscriptionAutoRenew = false;
    }

    /**
     * 결제 승인 시 PREMIUM(월간/연간) 구독을 시작한다(최초 구독). 자동갱신은 기본 켜짐 상태로 시작하고,
     * 오늘치 사용량도 즉시 채워줘서 자정 배치를 기다리지 않고 바로 이용할 수 있게 한다.
     */
    public void startPremiumSubscription(PlanType planType, LocalDateTime startAt, LocalDateTime endAt,
                                          int dailyTextLimit, int dailyImageLimit) {
        this.subscriptionPlan = planType;
        this.subscriptionStartAt = startAt;
        this.subscriptionEndAt = endAt;
        this.subscriptionAutoRenew = true;
        this.dailyTextRemaining = dailyTextLimit;
        this.dailyImageRemaining = dailyImageLimit;
        this.lastTokenResetDate = startAt.toLocalDate();
    }

    /**
     * 배치가 만료 시점에 자동갱신(Mock 재결제)을 시뮬레이션할 때 호출한다.
     * startPremiumSubscription과 달리 autoRenew 값은 건드리지 않는다(계속 켜진 상태 유지).
     */
    public void renewPremiumSubscription(LocalDateTime startAt, LocalDateTime endAt,
                                          int dailyTextLimit, int dailyImageLimit) {
        this.subscriptionStartAt = startAt;
        this.subscriptionEndAt = endAt;
        this.dailyTextRemaining = dailyTextLimit;
        this.dailyImageRemaining = dailyImageLimit;
        this.lastTokenResetDate = startAt.toLocalDate();
    }

    /**
     * 해지 예약. 즉시 FREE로 내리지 않고, 결제 주기 만료 시점에 배치가 자동갱신을 건너뛰고
     * downgradeToFree()를 호출하도록 표시만 해둔다.
     */
    public void reserveCancellation() {
        this.subscriptionAutoRenew = false;
    }

    /**
     * 해지 예약 취소(재개). 아직 혜택 기간이 남아있을 때 무료로 autoRenew만 다시 켠다.
     * 결제/기간 재계산 없이 원래 만료일에 다시 자동갱신되도록 되돌리는 것뿐이다.
     */
    public void resumeAutoRenew() {
        this.subscriptionAutoRenew = true;
    }

    /**
     * 해지 예약(autoRenew=false)된 회원의 결제 주기가 끝났을 때 배치가 호출해 FREE로 전환한다.
     */
    public void downgradeToFree() {
        this.subscriptionPlan = PlanType.FREE;
        this.subscriptionAutoRenew = false;
        this.dailyTextRemaining = 0;
        this.dailyImageRemaining = 0;
    }

    /**
     * PREMIUM 회원의 일일 생성 한도를 매일 자정 배치가 재충전할 때 호출한다.
     */
    public void resetDailyUsage(int dailyTextLimit, int dailyImageLimit) {
        this.dailyTextRemaining = dailyTextLimit;
        this.dailyImageRemaining = dailyImageLimit;
        this.lastTokenResetDate = LocalDate.now();
    }

    /**
     * 무료 체험(생애 1회, 책 1권) 소진 처리. 실제 소진 시점(책 생성 시작)은 book 도메인에서 호출한다.
     */
    public void useFreeTrial() {
        this.freeTrialUsed = true;
        this.freeTrialStarted = false;
    }

    /**
     * PREMIUM 텍스트/이미지 생성 1회 차감. 잔여량이 0 이하인지는 호출하는 서비스가 먼저 확인해야 한다.
     */
    public void decrementDailyText() {
        this.dailyTextRemaining = this.dailyTextRemaining - 1;
    }

    public void decrementDailyImage() {
        this.dailyImageRemaining = this.dailyImageRemaining - 1;
    }

    /**
     * 회원정보 수정(닉네임/프로필 이미지/소개). null인 필드는 건드리지 않는다.
     * 닉네임 중복 검사는 호출하는 서비스에서 먼저 확인해야 한다.
     */
    public void updateProfile(String nickname, String profileImageUrl, String introduction) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }
        if (introduction != null) {
            this.introduction = introduction;
        }
    }

    /**
     * 뷰어 글자 크기/페이지 전환 방식 환경설정 저장
     */
    public void updateViewerPreference(ViewerFontSize viewerFontSize, ViewerViewType viewerViewType) {
        if (viewerFontSize != null) {
            this.viewerFontSize = viewerFontSize;
        }
        if (viewerViewType != null) {
            this.viewerViewType = viewerViewType;
        }
    }
}
