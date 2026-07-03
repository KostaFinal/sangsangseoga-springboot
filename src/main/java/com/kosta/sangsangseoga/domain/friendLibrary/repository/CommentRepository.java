package com.kosta.sangsangseoga.domain.friendLibrary.repository;
 
import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.friendLibrary.entity.Comment;
import com.kosta.sangsangseoga.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 삭제되지 않은 댓글 조회 (수정/삭제 시 사용)
    java.util.Optional<Comment> findByIdAndIsDeletedFalse(Long id);

    // 회원 탈퇴 시 익명화 대상인 본인 작성 댓글 조회
    List<Comment> findAllByMember(Member member);

    // 책 삭제 시 해당 책의 댓글 전체 삭제
    void deleteAllByBook(Book book);
}