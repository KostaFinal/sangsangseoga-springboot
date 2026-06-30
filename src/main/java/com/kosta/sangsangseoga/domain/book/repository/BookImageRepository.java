package com.kosta.sangsangseoga.domain.book.repository;

import com.kosta.sangsangseoga.domain.book.entity.BookImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookImageRepository extends JpaRepository<BookImage, Long> {
}
