package com.kosta.sangsangseoga.domain.book.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QWeeklyBookRanking is a Querydsl query type for WeeklyBookRanking
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QWeeklyBookRanking extends EntityPathBase<WeeklyBookRanking> {

    private static final long serialVersionUID = -1818475904L;

    public static final QWeeklyBookRanking weeklyBookRanking = new QWeeklyBookRanking("weeklyBookRanking");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QWeeklyBookRanking(String variable) {
        super(WeeklyBookRanking.class, forVariable(variable));
    }

    public QWeeklyBookRanking(Path<? extends WeeklyBookRanking> path) {
        super(path.getType(), path.getMetadata());
    }

    public QWeeklyBookRanking(PathMetadata metadata) {
        super(WeeklyBookRanking.class, metadata);
    }

}

