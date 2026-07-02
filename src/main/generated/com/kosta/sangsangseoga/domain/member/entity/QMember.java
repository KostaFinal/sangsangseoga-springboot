package com.kosta.sangsangseoga.domain.member.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMember is a Querydsl query type for Member
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMember extends EntityPathBase<Member> {

    private static final long serialVersionUID = 1164749527L;

    public static final QMember member = new QMember("member1");

    public final com.kosta.sangsangseoga.global.common.QBaseEntity _super = new com.kosta.sangsangseoga.global.common.QBaseEntity(this);

    public final DatePath<java.time.LocalDate> birthDate = createDate("birthDate", java.time.LocalDate.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> dailyImageRemaining = createNumber("dailyImageRemaining", Integer.class);

    public final NumberPath<Integer> dailyTextRemaining = createNumber("dailyTextRemaining", Integer.class);

    public final StringPath email = createString("email");

    public final BooleanPath freeTrialUsed = createBoolean("freeTrialUsed");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath introduction = createString("introduction");

    public final DatePath<java.time.LocalDate> lastTokenResetDate = createDate("lastTokenResetDate", java.time.LocalDate.class);

    public final StringPath nickname = createString("nickname");

    public final StringPath password = createString("password");

    public final StringPath profileImageUrl = createString("profileImageUrl");

    public final EnumPath<com.kosta.sangsangseoga.domain.member.enums.MemberRole> role = createEnum("role", com.kosta.sangsangseoga.domain.member.enums.MemberRole.class);

    public final EnumPath<com.kosta.sangsangseoga.domain.member.enums.MemberStatus> status = createEnum("status", com.kosta.sangsangseoga.domain.member.enums.MemberStatus.class);

    public final DateTimePath<java.time.LocalDateTime> subscriptionEndAt = createDateTime("subscriptionEndAt", java.time.LocalDateTime.class);

    public final EnumPath<com.kosta.sangsangseoga.domain.subscription.enums.PlanType> subscriptionPlan = createEnum("subscriptionPlan", com.kosta.sangsangseoga.domain.subscription.enums.PlanType.class);

    public final DateTimePath<java.time.LocalDateTime> subscriptionStartAt = createDateTime("subscriptionStartAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final EnumPath<com.kosta.sangsangseoga.domain.member.enums.ViewerFontSize> viewerFontSize = createEnum("viewerFontSize", com.kosta.sangsangseoga.domain.member.enums.ViewerFontSize.class);

    public final EnumPath<com.kosta.sangsangseoga.domain.member.enums.ViewerViewType> viewerViewType = createEnum("viewerViewType", com.kosta.sangsangseoga.domain.member.enums.ViewerViewType.class);

    public final DateTimePath<java.time.LocalDateTime> withdrawnAt = createDateTime("withdrawnAt", java.time.LocalDateTime.class);

    public QMember(String variable) {
        super(Member.class, forVariable(variable));
    }

    public QMember(Path<? extends Member> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMember(PathMetadata metadata) {
        super(Member.class, metadata);
    }

}

