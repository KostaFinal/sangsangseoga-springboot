package com.kosta.sangsangseoga.domain.reading.repository;

import com.kosta.sangsangseoga.domain.reading.entity.MyReading;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyReadingRepository extends JpaRepository<MyReading, Long> {
}
