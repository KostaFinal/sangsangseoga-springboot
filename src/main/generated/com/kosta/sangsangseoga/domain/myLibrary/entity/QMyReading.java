package com.kosta.sangsangseoga.domain.myLibrary.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMyReading is a Querydsl query type for MyReading
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMyReading extends EntityPathBase<MyReading> {

    private static final long serialVersionUID = -1047294726L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMyReading myReading = new QMyReading("myReading");

    public final com.kosta.sangsangseoga.global.common.QBaseEntity _super = new com.kosta.sangsangseoga.global.common.QBaseEntity(this);

    public final com.kosta.sangsangseoga.domain.book.entity.QBook book;

    public final DateTimePath<java.time.LocalDateTime> completedAt = createDateTime("completedAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> currentPage = createNumber("currentPage", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.kosta.sangsangseoga.domain.member.entity.QMember member;

    public final NumberPath<Integer> progress = createNumber("progress", Integer.class);

    public final DatePath<java.time.LocalDate> readDate = createDate("readDate", java.time.LocalDate.class);

    public final EnumPath<com.kosta.sangsangseoga.domain.myLibrary.enums.ReadingStatus> readingStatus = createEnum("readingStatus", com.kosta.sangsangseoga.domain.myLibrary.enums.ReadingStatus.class);

    public final NumberPath<Integer> readingTime = createNumber("readingTime", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> recentReadAt = createDateTime("recentReadAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMyReading(String variable) {
        this(MyReading.class, forVariable(variable), INITS);
    }

    public QMyReading(Path<? extends MyReading> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMyReading(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMyReading(PathMetadata metadata, PathInits inits) {
        this(MyReading.class, metadata, inits);
    }

    public QMyReading(Class<? extends MyReading> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.book = inits.isInitialized("book") ? new com.kosta.sangsangseoga.domain.book.entity.QBook(forProperty("book"), inits.get("book")) : null;
        this.member = inits.isInitialized("member") ? new com.kosta.sangsangseoga.domain.member.entity.QMember(forProperty("member")) : null;
    }

}

