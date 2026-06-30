package com.kosta.sangsangseoga.domain.myLibrary.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;

import com.kosta.sangsangseoga.domain.myLibrary.entity.MyReading;
import com.querydsl.core.types.Path;


/**
 * QMyReading is a Querydsl query type for MyReading
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMyReading extends EntityPathBase<MyReading> {

    private static final long serialVersionUID = -1047294726L;

    public static final QMyReading myReading = new QMyReading("myReading");

    public final com.kosta.sangsangseoga.global.common.QBaseEntity _super = new com.kosta.sangsangseoga.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMyReading(String variable) {
        super(MyReading.class, forVariable(variable));
    }

    public QMyReading(Path<? extends MyReading> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMyReading(PathMetadata metadata) {
        super(MyReading.class, metadata);
    }

}

