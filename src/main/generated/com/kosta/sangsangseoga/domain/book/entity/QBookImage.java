package com.kosta.sangsangseoga.domain.book.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBookImage is a Querydsl query type for BookImage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBookImage extends EntityPathBase<BookImage> {

    private static final long serialVersionUID = -968171354L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBookImage bookImage = new QBookImage("bookImage");

    public final QBook book;

    public final NumberPath<Long> bookPageId = createNumber("bookPageId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final StringPath fileExtension = createString("fileExtension");

    public final StringPath fileName = createString("fileName");

    public final NumberPath<Long> fileSize = createNumber("fileSize", Long.class);

    public final StringPath fileUrl = createString("fileUrl");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> imageOrder = createNumber("imageOrder", Integer.class);

    public final EnumPath<BookImage.ImageType> imageType = createEnum("imageType", BookImage.ImageType.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QBookImage(String variable) {
        this(BookImage.class, forVariable(variable), INITS);
    }

    public QBookImage(Path<? extends BookImage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBookImage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBookImage(PathMetadata metadata, PathInits inits) {
        this(BookImage.class, metadata, inits);
    }

    public QBookImage(Class<? extends BookImage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.book = inits.isInitialized("book") ? new QBook(forProperty("book"), inits.get("book")) : null;
    }

}

