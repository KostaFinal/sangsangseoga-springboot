package com.kosta.sangsangseoga.domain.book.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBook is a Querydsl query type for Book
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBook extends EntityPathBase<Book> {

    private static final long serialVersionUID = -87645771L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBook book = new QBook("book");

    public final com.kosta.sangsangseoga.global.common.QBaseEntity _super = new com.kosta.sangsangseoga.global.common.QBaseEntity(this);

    public final EnumPath<com.kosta.sangsangseoga.domain.book.enums.AgeGroup> authorAgeGroup = createEnum("authorAgeGroup", com.kosta.sangsangseoga.domain.book.enums.AgeGroup.class);

    public final EnumPath<com.kosta.sangsangseoga.domain.book.enums.BookType> bookType = createEnum("bookType", com.kosta.sangsangseoga.domain.book.enums.BookType.class);

    public final StringPath category = createString("category");

    public final NumberPath<Integer> commentCount = createNumber("commentCount", Integer.class);

    public final StringPath confirmedSettings = createString("confirmedSettings");

    public final NumberPath<Long> coverImageId = createNumber("coverImageId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final EnumPath<com.kosta.sangsangseoga.domain.book.enums.CreationMode> creationMode = createEnum("creationMode", com.kosta.sangsangseoga.domain.book.enums.CreationMode.class);

    public final StringPath description = createString("description");

    public final StringPath genre = createString("genre");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> likeCount = createNumber("likeCount", Integer.class);

    public final com.kosta.sangsangseoga.domain.member.entity.QMember member;

    public final NumberPath<Integer> pageCount = createNumber("pageCount", Integer.class);

    public final EnumPath<com.kosta.sangsangseoga.domain.book.enums.AgeGroup> readerAgeGroup = createEnum("readerAgeGroup", com.kosta.sangsangseoga.domain.book.enums.AgeGroup.class);

    public final StringPath status = createString("status");

    public final StringPath styleCode = createString("styleCode");

    public final StringPath summary = createString("summary");

    public final StringPath targetLang = createString("targetLang");

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Integer> viewCount = createNumber("viewCount", Integer.class);

    public QBook(String variable) {
        this(Book.class, forVariable(variable), INITS);
    }

    public QBook(Path<? extends Book> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBook(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBook(PathMetadata metadata, PathInits inits) {
        this(Book.class, metadata, inits);
    }

    public QBook(Class<? extends Book> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.kosta.sangsangseoga.domain.member.entity.QMember(forProperty("member")) : null;
    }

}

