package com.kosta.sangsangseoga.domain.book.repository;

import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.book.entity.BookImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookImageRepository extends JpaRepository<BookImage, Long> {

    // 책의 표지 이미지 조회 (imageType = COVER)
    Optional<BookImage> findByBookAndImageTypeAndDeletedAtIsNull(Book book, BookImage.ImageType imageType);
}