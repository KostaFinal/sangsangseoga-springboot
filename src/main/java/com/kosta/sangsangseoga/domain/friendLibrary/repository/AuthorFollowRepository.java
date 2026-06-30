package com.kosta.sangsangseoga.domain.friendLibrary.repository;

import com.kosta.sangsangseoga.domain.friendLibrary.entity.AuthorFollow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorFollowRepository extends JpaRepository<AuthorFollow, Long> {
}
