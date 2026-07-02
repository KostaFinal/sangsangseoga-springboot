package com.kosta.sangsangseoga.domain.subscription.entity;

import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.subscription.enums.PaymentStatus;
import com.kosta.sangsangseoga.domain.subscription.enums.PlanType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
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
@EntityListeners(AuditingEntityListener.class)
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private Integer amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String failReason;

    private String pgTransactionId;

    private LocalDateTime paidAt;

    @Enumerated(EnumType.STRING)
    private PlanType planType;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Builder
    private Payment(Member member, Integer amount, PaymentStatus status, String failReason,
                     String pgTransactionId, LocalDateTime paidAt, PlanType planType) {
        this.member = member;
        this.amount = amount;
        this.status = status;
        this.failReason = failReason;
        this.pgTransactionId = pgTransactionId;
        this.paidAt = paidAt;
        this.planType = planType;
    }
}
