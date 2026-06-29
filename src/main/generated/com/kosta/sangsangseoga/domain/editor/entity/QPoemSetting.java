package com.kosta.sangsangseoga.domain.editor.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPoemSetting is a Querydsl query type for PoemSetting
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPoemSetting extends EntityPathBase<PoemSetting> {

    private static final long serialVersionUID = -936008295L;

    public static final QPoemSetting poemSetting = new QPoemSetting("poemSetting");

    public final com.kosta.sangsangseoga.global.common.QBaseEntity _super = new com.kosta.sangsangseoga.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QPoemSetting(String variable) {
        super(PoemSetting.class, forVariable(variable));
    }

    public QPoemSetting(Path<? extends PoemSetting> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPoemSetting(PathMetadata metadata) {
        super(PoemSetting.class, metadata);
    }

}

