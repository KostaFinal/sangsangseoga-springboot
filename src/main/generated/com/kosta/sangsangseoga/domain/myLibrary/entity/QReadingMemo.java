package com.kosta.sangsangseoga.domain.myLibrary.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReadingMemo is a Querydsl query type for ReadingMemo
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReadingMemo extends EntityPathBase<ReadingMemo> {

    private static final long serialVersionUID = 2111265056L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReadingMemo readingMemo = new QReadingMemo("readingMemo");

    public final com.kosta.sangsangseoga.global.common.QBaseEntity _super = new com.kosta.sangsangseoga.global.common.QBaseEntity(this);

    public final com.kosta.sangsangseoga.domain.book.entity.QBook book;

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.kosta.sangsangseoga.domain.account.entity.QMember member;

    public final NumberPath<Integer> pageNo = createNumber("pageNo", Integer.class);

    public final NumberPath<java.math.BigDecimal> posX = createNumber("posX", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> posY = createNumber("posY", java.math.BigDecimal.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QReadingMemo(String variable) {
        this(ReadingMemo.class, forVariable(variable), INITS);
    }

    public QReadingMemo(Path<? extends ReadingMemo> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReadingMemo(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReadingMemo(PathMetadata metadata, PathInits inits) {
        this(ReadingMemo.class, metadata, inits);
    }

    public QReadingMemo(Class<? extends ReadingMemo> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.book = inits.isInitialized("book") ? new com.kosta.sangsangseoga.domain.book.entity.QBook(forProperty("book"), inits.get("book")) : null;
        this.member = inits.isInitialized("member") ? new com.kosta.sangsangseoga.domain.account.entity.QMember(forProperty("member")) : null;
    }

}

