package com.kosta.sangsangseoga.domain.book.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBookImage is a Querydsl query type for BookImage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBookImage extends EntityPathBase<BookImage> {

    private static final long serialVersionUID = -968171354L;

    public static final QBookImage bookImage = new QBookImage("bookImage");

    public final com.kosta.sangsangseoga.global.common.QBaseEntity _super = new com.kosta.sangsangseoga.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QBookImage(String variable) {
        super(BookImage.class, forVariable(variable));
    }

    public QBookImage(Path<? extends BookImage> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBookImage(PathMetadata metadata) {
        super(BookImage.class, metadata);
    }

}

