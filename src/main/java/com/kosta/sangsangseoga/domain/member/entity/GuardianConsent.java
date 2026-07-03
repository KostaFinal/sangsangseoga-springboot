package com.kosta.sangsangseoga.domain.member.entity;

import com.kosta.sangsangseoga.domain.member.enums.GuardianConsentStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "guardian_consent")
public class GuardianConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String guardianEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_id")
    private Member guardian;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GuardianConsentStatus status;

    private LocalDateTime requestedAt;

    private LocalDateTime expiresAt;

    private LocalDateTime approvedAt;

    @Builder
    private GuardianConsent(Member member, String guardianEmail, Member guardian,
                              GuardianConsentStatus status, LocalDateTime requestedAt, LocalDateTime expiresAt) {
        this.member = member;
        this.guardianEmail = guardianEmail;
        this.guardian = guardian;
        this.status = status != null ? status : GuardianConsentStatus.REQUESTED;
        this.requestedAt = requestedAt;
        this.expiresAt = expiresAt;
    }

    public void approve(Member guardian) {
        this.status = GuardianConsentStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
        if (guardian != null) {
            this.guardian = guardian;
        }
    }

    public void reject() {
        this.status = GuardianConsentStatus.REJECTED;
    }

    public void expire() {
        this.status = GuardianConsentStatus.EXPIRED;
    }
}
