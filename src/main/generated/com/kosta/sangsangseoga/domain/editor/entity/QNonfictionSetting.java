package com.kosta.sangsangseoga.domain.editor.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QNonfictionSetting is a Querydsl query type for NonfictionSetting
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNonfictionSetting extends EntityPathBase<NonfictionSetting> {

    private static final long serialVersionUID = 1675205849L;

    public static final QNonfictionSetting nonfictionSetting = new QNonfictionSetting("nonfictionSetting");

    public final com.kosta.sangsangseoga.global.common.QBaseEntity _super = new com.kosta.sangsangseoga.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QNonfictionSetting(String variable) {
        super(NonfictionSetting.class, forVariable(variable));
    }

    public QNonfictionSetting(Path<? extends NonfictionSetting> path) {
        super(path.getType(), path.getMetadata());
    }

    public QNonfictionSetting(PathMetadata metadata) {
        super(NonfictionSetting.class, metadata);
    }

}

