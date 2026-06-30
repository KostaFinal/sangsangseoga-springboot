package com.kosta.sangsangseoga.domain.myLibrary.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBookReview is a Querydsl query type for BookReview
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBookReview extends EntityPathBase<BookReview> {

    private static final long serialVersionUID = 258774311L;

    public static final QBookReview bookReview = new QBookReview("bookReview");

    public final com.kosta.sangsangseoga.global.common.QBaseEntity _super = new com.kosta.sangsangseoga.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QBookReview(String variable) {
        super(BookReview.class, forVariable(variable));
    }

    public QBookReview(Path<? extends BookReview> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBookReview(PathMetadata metadata) {
        super(BookReview.class, metadata);
    }

}

