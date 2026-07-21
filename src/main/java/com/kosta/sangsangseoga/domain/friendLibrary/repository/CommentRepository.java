package com.kosta.sangsangseoga.domain.friendLibrary.repository;
 
import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.friendLibrary.entity.Comment;
import com.kosta.sangsangseoga.domain.member.entity.Member;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 삭제되지 않은 댓글 조회 (수정/삭제 시 사용)
    Optional<Comment> findByIdAndIsDeletedFalse(Long id);

    // 회원 탈퇴 시 익명화 대상인 본인 작성 댓글 조회
    List<Comment> findAllByMember(Member member);

    // 책 삭제 시 해당 책의 댓글 전체 삭제
    void deleteAllByBook(Book book);
    
    // cursor 없을 때 - 첫 페이지 (createdAt DESC)
    @Query("SELECT c FROM Comment c WHERE c.book = :book AND c.isDeleted = false ORDER BY c.createdAt DESC, c.id DESC")
    List<Comment> findByBookOrderByCreatedAtDesc(@Param("book") Book book, Pageable pageable);
 
    // cursor 있을 때 - cursor ID보다 작은 것 (다음 페이지)
    @Query("SELECT c FROM Comment c WHERE c.book = :book AND c.isDeleted = false AND c.id < :cursorId ORDER BY c.createdAt DESC, c.id DESC")
    List<Comment> findByBookAndIdLessThanOrderByCreatedAtDesc(@Param("book") Book book, @Param("cursorId") Long cursorId, Pageable pageable);
}