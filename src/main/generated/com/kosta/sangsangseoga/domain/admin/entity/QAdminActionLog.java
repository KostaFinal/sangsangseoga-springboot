package com.kosta.sangsangseoga.domain.admin.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAdminActionLog is a Querydsl query type for AdminActionLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAdminActionLog extends EntityPathBase<AdminActionLog> {

    private static final long serialVersionUID = -144439291L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAdminActionLog adminActionLog = new QAdminActionLog("adminActionLog");

    public final StringPath actionReason = createString("actionReason");

    public final EnumPath<com.kosta.sangsangseoga.domain.admin.enums.AdminActionType> actionType = createEnum("actionType", com.kosta.sangsangseoga.domain.admin.enums.AdminActionType.class);

    public final com.kosta.sangsangseoga.domain.member.entity.QMember admin;

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.kosta.sangsangseoga.domain.friendLibrary.entity.QReport report;

    public QAdminActionLog(String variable) {
        this(AdminActionLog.class, forVariable(variable), INITS);
    }

    public QAdminActionLog(Path<? extends AdminActionLog> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAdminActionLog(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAdminActionLog(PathMetadata metadata, PathInits inits) {
        this(AdminActionLog.class, metadata, inits);
    }

    public QAdminActionLog(Class<? extends AdminActionLog> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.admin = inits.isInitialized("admin") ? new com.kosta.sangsangseoga.domain.member.entity.QMember(forProperty("admin")) : null;
        this.report = inits.isInitialized("report") ? new com.kosta.sangsangseoga.domain.friendLibrary.entity.QReport(forProperty("report"), inits.get("report")) : null;
    }

}

