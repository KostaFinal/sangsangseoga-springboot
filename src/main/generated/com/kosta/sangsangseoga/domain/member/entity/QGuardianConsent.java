package com.kosta.sangsangseoga.domain.member.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGuardianConsent is a Querydsl query type for GuardianConsent
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGuardianConsent extends EntityPathBase<GuardianConsent> {

    private static final long serialVersionUID = -1816937044L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGuardianConsent guardianConsent = new QGuardianConsent("guardianConsent");

    public final DateTimePath<java.time.LocalDateTime> approvedAt = createDateTime("approvedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> expiresAt = createDateTime("expiresAt", java.time.LocalDateTime.class);

    public final QMember guardian;

    public final StringPath guardianEmail = createString("guardianEmail");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QMember member;

    public final DateTimePath<java.time.LocalDateTime> requestedAt = createDateTime("requestedAt", java.time.LocalDateTime.class);

    public final EnumPath<com.kosta.sangsangseoga.domain.member.enums.GuardianConsentStatus> status = createEnum("status", com.kosta.sangsangseoga.domain.member.enums.GuardianConsentStatus.class);

    public QGuardianConsent(String variable) {
        this(GuardianConsent.class, forVariable(variable), INITS);
    }

    public QGuardianConsent(Path<? extends GuardianConsent> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGuardianConsent(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGuardianConsent(PathMetadata metadata, PathInits inits) {
        this(GuardianConsent.class, metadata, inits);
    }

    public QGuardianConsent(Class<? extends GuardianConsent> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.guardian = inits.isInitialized("guardian") ? new QMember(forProperty("guardian")) : null;
        this.member = inits.isInitialized("member") ? new QMember(forProperty("member")) : null;
    }

}

