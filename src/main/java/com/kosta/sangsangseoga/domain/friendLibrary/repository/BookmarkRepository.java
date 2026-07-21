package com.kosta.sangsangseoga.domain.friendLibrary.repository;

import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.friendLibrary.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    // 회원이 특정 책을 북마크했는지 확인
    boolean existsByMemberAndBook(Member member, Book book);

    // 회원의 특정 책 북마크 row 조회 (삭제 시 사용)
    Optional<Bookmark> findByMemberAndBook(Member member, Book book);

    // 회원 탈퇴 시 해당 회원의 책갈피 전체 삭제
    void deleteAllByMember(Member member);

    // 책 삭제 시 해당 책의 책갈피 전체 삭제
    void deleteAllByBook(Book book);
}