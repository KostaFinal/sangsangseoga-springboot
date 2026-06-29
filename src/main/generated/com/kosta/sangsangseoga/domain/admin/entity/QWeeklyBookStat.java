package com.kosta.sangsangseoga.domain.admin.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QWeeklyBookStat is a Querydsl query type for WeeklyBookStat
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QWeeklyBookStat extends EntityPathBase<WeeklyBookStat> {

    private static final long serialVersionUID = 1594562020L;

    public static final QWeeklyBookStat weeklyBookStat = new QWeeklyBookStat("weeklyBookStat");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QWeeklyBookStat(String variable) {
        super(WeeklyBookStat.class, forVariable(variable));
    }

    public QWeeklyBookStat(Path<? extends WeeklyBookStat> path) {
        super(path.getType(), path.getMetadata());
    }

    public QWeeklyBookStat(PathMetadata metadata) {
        super(WeeklyBookStat.class, metadata);
    }

}

