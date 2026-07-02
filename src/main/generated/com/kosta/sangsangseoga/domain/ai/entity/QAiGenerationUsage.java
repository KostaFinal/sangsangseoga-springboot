package com.kosta.sangsangseoga.domain.ai.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAiGenerationUsage is a Querydsl query type for AiGenerationUsage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAiGenerationUsage extends EntityPathBase<AiGenerationUsage> {

    private static final long serialVersionUID = -432154378L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAiGenerationUsage aiGenerationUsage = new QAiGenerationUsage("aiGenerationUsage");

    public final com.kosta.sangsangseoga.domain.book.entity.QBook book;

    public final EnumPath<com.kosta.sangsangseoga.domain.ai.enums.CallType> callType = createEnum("callType", com.kosta.sangsangseoga.domain.ai.enums.CallType.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> imageCount = createNumber("imageCount", Integer.class);

    public final NumberPath<Integer> inputTokenCount = createNumber("inputTokenCount", Integer.class);

    public final com.kosta.sangsangseoga.domain.member.entity.QMember member;

    public final NumberPath<Integer> outputTokenCount = createNumber("outputTokenCount", Integer.class);

    public QAiGenerationUsage(String variable) {
        this(AiGenerationUsage.class, forVariable(variable), INITS);
    }

    public QAiGenerationUsage(Path<? extends AiGenerationUsage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAiGenerationUsage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAiGenerationUsage(PathMetadata metadata, PathInits inits) {
        this(AiGenerationUsage.class, metadata, inits);
    }

    public QAiGenerationUsage(Class<? extends AiGenerationUsage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.book = inits.isInitialized("book") ? new com.kosta.sangsangseoga.domain.book.entity.QBook(forProperty("book"), inits.get("book")) : null;
        this.member = inits.isInitialized("member") ? new com.kosta.sangsangseoga.domain.member.entity.QMember(forProperty("member")) : null;
    }

}

