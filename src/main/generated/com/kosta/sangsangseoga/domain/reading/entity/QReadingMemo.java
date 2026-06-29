package com.kosta.sangsangseoga.domain.reading.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QReadingMemo is a Querydsl query type for ReadingMemo
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReadingMemo extends EntityPathBase<ReadingMemo> {

    private static final long serialVersionUID = -1206308253L;

    public static final QReadingMemo readingMemo = new QReadingMemo("readingMemo");

    public final com.kosta.sangsangseoga.global.common.QBaseEntity _super = new com.kosta.sangsangseoga.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QReadingMemo(String variable) {
        super(ReadingMemo.class, forVariable(variable));
    }

    public QReadingMemo(Path<? extends ReadingMemo> path) {
        super(path.getType(), path.getMetadata());
    }

    public QReadingMemo(PathMetadata metadata) {
        super(ReadingMemo.class, metadata);
    }

}

