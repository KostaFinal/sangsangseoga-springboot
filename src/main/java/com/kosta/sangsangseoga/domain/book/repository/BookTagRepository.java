package com.kosta.sangsangseoga.domain.book.repository;

import com.kosta.sangsangseoga.domain.book.entity.BookTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookTagRepository extends JpaRepository<BookTag, Long> {
}
