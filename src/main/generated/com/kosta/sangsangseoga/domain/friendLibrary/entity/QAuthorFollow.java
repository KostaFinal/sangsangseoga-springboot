package com.kosta.sangsangseoga.domain.friendLibrary.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAuthorFollow is a Querydsl query type for AuthorFollow
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAuthorFollow extends EntityPathBase<AuthorFollow> {

    private static final long serialVersionUID = 1801847792L;

    public static final QAuthorFollow authorFollow = new QAuthorFollow("authorFollow");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QAuthorFollow(String variable) {
        super(AuthorFollow.class, forVariable(variable));
    }

    public QAuthorFollow(Path<? extends AuthorFollow> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAuthorFollow(PathMetadata metadata) {
        super(AuthorFollow.class, metadata);
    }

}

