package com.kosta.sangsangseoga.domain.friendLibrary.repository;

import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.friendLibrary.entity.BookLike;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookLikeRepository extends JpaRepository<BookLike, Long> {
	
	 // ȸ���� �ش� å�� ���ƿ並 �������� ���� Ȯ��
    boolean existsByMemberAndBook(Member member, Book book);
 
    // ȸ���� å���� ���ƿ� row ��ȸ (���ƿ� ��� �� ���)
    Optional<BookLike> findByMemberAndBook(Member member, Book book);

    // 회원 탈퇴 시 해당 회원의 좋아요 전체 삭제
    void deleteAllByMember(Member member);

    // 책 삭제 시 해당 책의 좋아요 전체 삭제
    void deleteAllByBook(Book book);
}
