package com.kosta.sangsangseoga.domain.myLibrary.repository;

import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.myLibrary.entity.ReadingMemo;

import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.Optional;
 
public interface ReadingMemoRepository extends JpaRepository<ReadingMemo, Long> {
 
    // 회원의 특정 책 특정 페이지 메모 조회
    Optional<ReadingMemo> findByMemberAndBookAndPageNo(Member member, Book book, Integer pageNo);
 
    // 회원의 특정 책 특정 페이지 메모 존재 여부 확인 (중복 방지)
    boolean existsByMemberAndBookAndPageNo(Member member, Book book, Integer pageNo);

    // 책 삭제 시 해당 책의 메모 전체 삭제
    void deleteAllByBook(Book book);
}