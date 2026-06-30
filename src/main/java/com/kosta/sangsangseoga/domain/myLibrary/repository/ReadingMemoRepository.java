package com.kosta.sangsangseoga.domain.myLibrary.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kosta.sangsangseoga.domain.myLibrary.entity.ReadingMemo;

public interface ReadingMemoRepository extends JpaRepository<ReadingMemo, Long> {
}
