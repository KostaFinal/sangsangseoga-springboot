package com.kosta.sangsangseoga.domain.book.repository;

import com.kosta.sangsangseoga.domain.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
