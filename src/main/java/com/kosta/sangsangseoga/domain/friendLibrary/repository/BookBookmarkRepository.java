package com.kosta.sangsangseoga.domain.friendLibrary.repository;

import com.kosta.sangsangseoga.domain.friendLibrary.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookBookmarkRepository extends JpaRepository<Bookmark, Long> {
}
