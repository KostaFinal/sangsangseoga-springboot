package com.kosta.sangsangseoga.domain.friendLibrary.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReport is a Querydsl query type for Report
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReport extends EntityPathBase<Report> {

    private static final long serialVersionUID = 409359112L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReport report = new QReport("report");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> processedAt = createDateTime("processedAt", java.time.LocalDateTime.class);

    public final com.kosta.sangsangseoga.domain.account.entity.QMember processedBy;

    public final EnumPath<com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportReason> reason = createEnum("reason", com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportReason.class);

    public final StringPath reasonDetail = createString("reasonDetail");

    public final com.kosta.sangsangseoga.domain.account.entity.QMember reporter;

    public final EnumPath<com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportStatus> status = createEnum("status", com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportStatus.class);

    public final NumberPath<Long> targetId = createNumber("targetId", Long.class);

    public final EnumPath<com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportTargetType> targetType = createEnum("targetType", com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportTargetType.class);

    public QReport(String variable) {
        this(Report.class, forVariable(variable), INITS);
    }

    public QReport(Path<? extends Report> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReport(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReport(PathMetadata metadata, PathInits inits) {
        this(Report.class, metadata, inits);
    }

    public QReport(Class<? extends Report> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.processedBy = inits.isInitialized("processedBy") ? new com.kosta.sangsangseoga.domain.account.entity.QMember(forProperty("processedBy")) : null;
        this.reporter = inits.isInitialized("reporter") ? new com.kosta.sangsangseoga.domain.account.entity.QMember(forProperty("reporter")) : null;
    }

}

