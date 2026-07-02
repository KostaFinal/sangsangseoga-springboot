package com.kosta.sangsangseoga.domain.friendLibrary.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAuthorFollow is a Querydsl query type for AuthorFollow
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAuthorFollow extends EntityPathBase<AuthorFollow> {

    private static final long serialVersionUID = 1801847792L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAuthorFollow authorFollow = new QAuthorFollow("authorFollow");

    public final com.kosta.sangsangseoga.domain.account.entity.QMember author;

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final com.kosta.sangsangseoga.domain.account.entity.QMember follower;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QAuthorFollow(String variable) {
        this(AuthorFollow.class, forVariable(variable), INITS);
    }

    public QAuthorFollow(Path<? extends AuthorFollow> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAuthorFollow(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAuthorFollow(PathMetadata metadata, PathInits inits) {
        this(AuthorFollow.class, metadata, inits);
    }

    public QAuthorFollow(Class<? extends AuthorFollow> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.author = inits.isInitialized("author") ? new com.kosta.sangsangseoga.domain.account.entity.QMember(forProperty("author")) : null;
        this.follower = inits.isInitialized("follower") ? new com.kosta.sangsangseoga.domain.account.entity.QMember(forProperty("follower")) : null;
    }

}

