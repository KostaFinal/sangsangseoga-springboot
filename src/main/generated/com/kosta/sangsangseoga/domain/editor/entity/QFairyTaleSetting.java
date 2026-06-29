package com.kosta.sangsangseoga.domain.editor.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFairyTaleSetting is a Querydsl query type for FairyTaleSetting
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFairyTaleSetting extends EntityPathBase<FairyTaleSetting> {

    private static final long serialVersionUID = 1760355205L;

    public static final QFairyTaleSetting fairyTaleSetting = new QFairyTaleSetting("fairyTaleSetting");

    public final com.kosta.sangsangseoga.global.common.QBaseEntity _super = new com.kosta.sangsangseoga.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QFairyTaleSetting(String variable) {
        super(FairyTaleSetting.class, forVariable(variable));
    }

    public QFairyTaleSetting(Path<? extends FairyTaleSetting> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFairyTaleSetting(PathMetadata metadata) {
        super(FairyTaleSetting.class, metadata);
    }

}

