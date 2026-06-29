package com.kosta.sangsangseoga.domain.book.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBookPage is a Querydsl query type for BookPage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBookPage extends EntityPathBase<BookPage> {

    private static final long serialVersionUID = 246060516L;

    public static final QBookPage bookPage = new QBookPage("bookPage");

    public final com.kosta.sangsangseoga.global.common.QBaseEntity _super = new com.kosta.sangsangseoga.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QBookPage(String variable) {
        super(BookPage.class, forVariable(variable));
    }

    public QBookPage(Path<? extends BookPage> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBookPage(PathMetadata metadata) {
        super(BookPage.class, metadata);
    }

}

