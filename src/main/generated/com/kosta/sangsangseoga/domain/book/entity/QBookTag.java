package com.kosta.sangsangseoga.domain.book.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBookTag is a Querydsl query type for BookTag
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBookTag extends EntityPathBase<BookTag> {

    private static final long serialVersionUID = 285035941L;

    public static final QBookTag bookTag = new QBookTag("bookTag");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QBookTag(String variable) {
        super(BookTag.class, forVariable(variable));
    }

    public QBookTag(Path<? extends BookTag> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBookTag(PathMetadata metadata) {
        super(BookTag.class, metadata);
    }

}

