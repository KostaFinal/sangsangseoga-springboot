package com.kosta.sangsangseoga.domain.book.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QWeeklyBookRanking is a Querydsl query type for WeeklyBookRanking
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QWeeklyBookRanking extends EntityPathBase<WeeklyBookRanking> {

    private static final long serialVersionUID = -1818475904L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QWeeklyBookRanking weeklyBookRanking = new QWeeklyBookRanking("weeklyBookRanking");

    public final QBook book;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> score = createNumber("score", Integer.class);

    public final DatePath<java.time.LocalDate> weekStartDate = createDate("weekStartDate", java.time.LocalDate.class);

    public QWeeklyBookRanking(String variable) {
        this(WeeklyBookRanking.class, forVariable(variable), INITS);
    }

    public QWeeklyBookRanking(Path<? extends WeeklyBookRanking> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QWeeklyBookRanking(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QWeeklyBookRanking(PathMetadata metadata, PathInits inits) {
        this(WeeklyBookRanking.class, metadata, inits);
    }

    public QWeeklyBookRanking(Class<? extends WeeklyBookRanking> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.book = inits.isInitialized("book") ? new QBook(forProperty("book"), inits.get("book")) : null;
    }

}

