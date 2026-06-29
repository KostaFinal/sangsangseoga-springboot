package com.kosta.sangsangseoga.domain.subscription.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSubscription is a Querydsl query type for Subscription
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSubscription extends EntityPathBase<Subscription> {

    private static final long serialVersionUID = 1897253405L;

    public static final QSubscription subscription = new QSubscription("subscription");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QSubscription(String variable) {
        super(Subscription.class, forVariable(variable));
    }

    public QSubscription(Path<? extends Subscription> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSubscription(PathMetadata metadata) {
        super(Subscription.class, metadata);
    }

}

