package com.kosta.sangsangseoga.domain.friendLibrary.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBookBookmark is a Querydsl query type for BookBookmark
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBookBookmark extends EntityPathBase<BookBookmark> {

    private static final long serialVersionUID = 1368431123L;

    public static final QBookBookmark bookBookmark = new QBookBookmark("bookBookmark");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QBookBookmark(String variable) {
        super(BookBookmark.class, forVariable(variable));
    }

    public QBookBookmark(Path<? extends BookBookmark> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBookBookmark(PathMetadata metadata) {
        super(BookBookmark.class, metadata);
    }

}

