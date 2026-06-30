package com.kosta.sangsangseoga.domain.reading.repository;

import com.kosta.sangsangseoga.domain.reading.entity.ReadingMemo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingMemoRepository extends JpaRepository<ReadingMemo, Long> {
}
