package com.kosta.sangsangseoga.domain.book.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBookPage is a Querydsl query type for BookPage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBookPage extends EntityPathBase<BookPage> {

    private static final long serialVersionUID = 246060516L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBookPage bookPage = new QBookPage("bookPage");

    public final com.kosta.sangsangseoga.global.common.QBaseEntity _super = new com.kosta.sangsangseoga.global.common.QBaseEntity(this);

    public final QBook book;

    public final StringPath contentTextEn = createString("contentTextEn");

    public final StringPath contentTextKo = createString("contentTextKo");

    public final EnumPath<BookPage.ContentType> contentType = createEnum("contentType", BookPage.ContentType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final NumberPath<Integer> pageNo = createNumber("pageNo", Integer.class);

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QBookPage(String variable) {
        this(BookPage.class, forVariable(variable), INITS);
    }

    public QBookPage(Path<? extends BookPage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBookPage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBookPage(PathMetadata metadata, PathInits inits) {
        this(BookPage.class, metadata, inits);
    }

    public QBookPage(Class<? extends BookPage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.book = inits.isInitialized("book") ? new QBook(forProperty("book"), inits.get("book")) : null;
    }

}

