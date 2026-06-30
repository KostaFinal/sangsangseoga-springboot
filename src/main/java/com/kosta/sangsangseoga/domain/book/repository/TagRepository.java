package com.kosta.sangsangseoga.domain.book.repository;

import com.kosta.sangsangseoga.domain.book.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
}
