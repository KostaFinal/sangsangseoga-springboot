package com.kosta.sangsangseoga.domain.editor.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QEssaySetting is a Querydsl query type for EssaySetting
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEssaySetting extends EntityPathBase<EssaySetting> {

    private static final long serialVersionUID = 1072133347L;

    public static final QEssaySetting essaySetting = new QEssaySetting("essaySetting");

    public final com.kosta.sangsangseoga.global.common.QBaseEntity _super = new com.kosta.sangsangseoga.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QEssaySetting(String variable) {
        super(EssaySetting.class, forVariable(variable));
    }

    public QEssaySetting(Path<? extends EssaySetting> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEssaySetting(PathMetadata metadata) {
        super(EssaySetting.class, metadata);
    }

}

