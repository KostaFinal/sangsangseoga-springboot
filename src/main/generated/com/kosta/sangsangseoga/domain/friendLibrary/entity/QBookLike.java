package com.kosta.sangsangseoga.domain.friendLibrary.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBookLike is a Querydsl query type for BookLike
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBookLike extends EntityPathBase<BookLike> {

    private static final long serialVersionUID = 688560884L;

    public static final QBookLike bookLike = new QBookLike("bookLike");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QBookLike(String variable) {
        super(BookLike.class, forVariable(variable));
    }

    public QBookLike(Path<? extends BookLike> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBookLike(PathMetadata metadata) {
        super(BookLike.class, metadata);
    }

}

