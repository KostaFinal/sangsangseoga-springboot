package com.kosta.sangsangseoga.domain.ai.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAiGenerationUsage is a Querydsl query type for AiGenerationUsage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAiGenerationUsage extends EntityPathBase<AiGenerationUsage> {

    private static final long serialVersionUID = -432154378L;

    public static final QAiGenerationUsage aiGenerationUsage = new QAiGenerationUsage("aiGenerationUsage");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QAiGenerationUsage(String variable) {
        super(AiGenerationUsage.class, forVariable(variable));
    }

    public QAiGenerationUsage(Path<? extends AiGenerationUsage> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAiGenerationUsage(PathMetadata metadata) {
        super(AiGenerationUsage.class, metadata);
    }

}

