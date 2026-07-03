package com.kosta.sangsangseoga.domain.friendLibrary.service;
 
import com.kosta.sangsangseoga.domain.friendLibrary.dto.CommentDto;
 
 
public interface CommentService {
 
    // 댓글 작성 - 201 응답
    CommentDto addComment(Long memberId, Long bookId, String content, Long replyToCommentId) throws Exception;
 
    // 답글 작성 - 201 응답
    CommentDto addReply(Long memberId, Long commentId, String content) throws Exception;
 
    // 댓글 수정 - 200 응답, 작성자만 가능
    void updateComment(Long memberId, Long commentId, String content) throws Exception;
 
    // 댓글 삭제 - 204 응답, 소프트 딜리트, 작성자만 가능
    void deleteComment(Long memberId, Long commentId) throws Exception;
}