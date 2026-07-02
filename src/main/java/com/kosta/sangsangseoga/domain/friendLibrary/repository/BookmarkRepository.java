package com.kosta.sangsangseoga.domain.friendLibrary.repository;

import com.kosta.sangsangseoga.domain.account.entity.Member;
import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.friendLibrary.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    // 회원의 특정 책 특정 페이지 책갈피 존재 여부 확인
    boolean existsByMemberAndBookAndPageNo(Member member, Book book, Integer pageNo);

    // 회원의 특정 책 특정 페이지 책갈피 조회 (삭제 시 사용)
    Optional<Bookmark> findByMemberAndBookAndPageNo(Member member, Book book, Integer pageNo);

    // 회원의 특정 책 전체 책갈피 목록 조회
    List<Bookmark> findAllByMemberAndBook(Member member, Book book);
}