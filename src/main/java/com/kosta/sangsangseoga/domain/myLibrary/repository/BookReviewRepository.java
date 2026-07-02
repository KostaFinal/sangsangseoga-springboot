package com.kosta.sangsangseoga.domain.myLibrary.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kosta.sangsangseoga.domain.myLibrary.entity.BookReview;

public interface BookReviewRepository extends JpaRepository<BookReview, Long> {
}
