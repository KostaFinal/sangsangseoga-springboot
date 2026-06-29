package com.kosta.sangsangseoga.domain.ai.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QDailyTokenUsageSnapshot is a Querydsl query type for DailyTokenUsageSnapshot
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDailyTokenUsageSnapshot extends EntityPathBase<DailyTokenUsageSnapshot> {

    private static final long serialVersionUID = 1133992474L;

    public static final QDailyTokenUsageSnapshot dailyTokenUsageSnapshot = new QDailyTokenUsageSnapshot("dailyTokenUsageSnapshot");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QDailyTokenUsageSnapshot(String variable) {
        super(DailyTokenUsageSnapshot.class, forVariable(variable));
    }

    public QDailyTokenUsageSnapshot(Path<? extends DailyTokenUsageSnapshot> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDailyTokenUsageSnapshot(PathMetadata metadata) {
        super(DailyTokenUsageSnapshot.class, metadata);
    }

}

