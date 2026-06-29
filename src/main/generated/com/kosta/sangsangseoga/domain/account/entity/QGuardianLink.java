package com.kosta.sangsangseoga.domain.account.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QGuardianLink is a Querydsl query type for GuardianLink
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGuardianLink extends EntityPathBase<GuardianLink> {

    private static final long serialVersionUID = 1888247151L;

    public static final QGuardianLink guardianLink = new QGuardianLink("guardianLink");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QGuardianLink(String variable) {
        super(GuardianLink.class, forVariable(variable));
    }

    public QGuardianLink(Path<? extends GuardianLink> path) {
        super(path.getType(), path.getMetadata());
    }

    public QGuardianLink(PathMetadata metadata) {
        super(GuardianLink.class, metadata);
    }

}

