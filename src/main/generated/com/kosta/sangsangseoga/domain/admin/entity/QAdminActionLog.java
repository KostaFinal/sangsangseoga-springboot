package com.kosta.sangsangseoga.domain.admin.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAdminActionLog is a Querydsl query type for AdminActionLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAdminActionLog extends EntityPathBase<AdminActionLog> {

    private static final long serialVersionUID = -144439291L;

    public static final QAdminActionLog adminActionLog = new QAdminActionLog("adminActionLog");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QAdminActionLog(String variable) {
        super(AdminActionLog.class, forVariable(variable));
    }

    public QAdminActionLog(Path<? extends AdminActionLog> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAdminActionLog(PathMetadata metadata) {
        super(AdminActionLog.class, metadata);
    }

}

