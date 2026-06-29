package com.kosta.sangsangseoga.domain.editor.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QNovelSetting is a Querydsl query type for NovelSetting
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNovelSetting extends EntityPathBase<NovelSetting> {

    private static final long serialVersionUID = 420627012L;

    public static final QNovelSetting novelSetting = new QNovelSetting("novelSetting");

    public final com.kosta.sangsangseoga.global.common.QBaseEntity _super = new com.kosta.sangsangseoga.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QNovelSetting(String variable) {
        super(NovelSetting.class, forVariable(variable));
    }

    public QNovelSetting(Path<? extends NovelSetting> path) {
        super(path.getType(), path.getMetadata());
    }

    public QNovelSetting(PathMetadata metadata) {
        super(NovelSetting.class, metadata);
    }

}

