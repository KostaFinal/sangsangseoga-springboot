package com.kosta.sangsangseoga.domain.friendLibrary.repository;
 
import com.kosta.sangsangseoga.domain.friendLibrary.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
 
public interface CommentRepository extends JpaRepository<Comment, Long> {
 
    // 삭제되지 않은 댓글 조회 (수정/삭제 시 사용)
    java.util.Optional<Comment> findByIdAndIsDeletedFalse(Long id);
}