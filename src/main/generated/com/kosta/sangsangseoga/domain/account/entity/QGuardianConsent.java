package com.kosta.sangsangseoga.domain.account.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QGuardianConsent is a Querydsl query type for GuardianConsent
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGuardianConsent extends EntityPathBase<GuardianConsent> {

    private static final long serialVersionUID = -1936252283L;

    public static final QGuardianConsent guardianConsent = new QGuardianConsent("guardianConsent");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QGuardianConsent(String variable) {
        super(GuardianConsent.class, forVariable(variable));
    }

    public QGuardianConsent(Path<? extends GuardianConsent> path) {
        super(path.getType(), path.getMetadata());
    }

    public QGuardianConsent(PathMetadata metadata) {
        super(GuardianConsent.class, metadata);
    }

}

