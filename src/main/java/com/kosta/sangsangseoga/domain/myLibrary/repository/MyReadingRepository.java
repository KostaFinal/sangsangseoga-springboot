package com.kosta.sangsangseoga.domain.myLibrary.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kosta.sangsangseoga.domain.myLibrary.entity.MyReading;

public interface MyReadingRepository extends JpaRepository<MyReading, Long> {
}
