package com.kosta.sangsangseoga.domain.friendLibrary.repository;

import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.friendLibrary.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

	// 회원의 특정 책 특정 페이지 책갈피 존재 여부 확인
    boolean existsByMemberAndBookAndPageNo(Member member, Book book, Integer pageNo);

    // 회원의 특정 책에 북마크가 하나라도 있는지 확인 (상세 조회 isBookmarkedByMe용)
    boolean existsByMemberAndBook(Member member, Book book);

    // 회원의 특정 책 특정 페이지 책갈피 조회 (삭제 시 사용)
    Optional<Bookmark> findByMemberAndBookAndPageNo(Member member, Book book, Integer pageNo);

    // 회원의 특정 책 전체 책갈피 목록 조회
    List<Bookmark> findAllByMemberAndBook(Member member, Book book);

    // 회원 탈퇴 시 해당 회원의 책갈피 전체 삭제
    void deleteAllByMember(Member member);

    // 책 삭제 시 해당 책의 책갈피 전체 삭제
    void deleteAllByBook(Book book);
}