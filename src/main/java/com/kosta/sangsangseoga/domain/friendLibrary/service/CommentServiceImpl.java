package com.kosta.sangsangseoga.domain.friendLibrary.service;
 
import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.book.repository.BookRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.dto.CommentDto;
import com.kosta.sangsangseoga.domain.friendLibrary.entity.Comment;
import com.kosta.sangsangseoga.domain.friendLibrary.exception.FriendLibraryErrorCode;
import com.kosta.sangsangseoga.domain.friendLibrary.repository.CommentRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.service.CommentService;
import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {
 
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
 
    /**
     * 댓글 작성
     * - replyToCommentId가 있으면 답글, 없으면 일반 댓글
     * - 답글의 답글 방지 (1depth만 허용)
     */
    @Override
    public CommentDto addComment(Long memberId, Long bookId, String content, Long replyToCommentId) throws Exception {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));
 
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.BOOK_NOT_FOUND));
 
        Comment replyTo = null;
        if (replyToCommentId != null) {
            replyTo = commentRepository.findByIdAndIsDeletedFalse(replyToCommentId)
                    .orElseThrow(() -> new CustomException(FriendLibraryErrorCode.COMMENT_NOT_FOUND));
 
            // 답글의 답글 방지 (1depth만 허용)
            if (replyTo.getReplyTo() != null) {
                throw new CustomException(FriendLibraryErrorCode.REPLY_DEPTH_EXCEEDED);
            }
        }
 
        Comment comment = commentRepository.save(Comment.builder()
                .member(member)
                .book(book)
                .content(content)
                .replyTo(replyTo)
                .isDeleted(false)
                .build());
 
        // book의 댓글 수 증가
        book.setCommentCount(book.getCommentCount() + 1);
 
        return CommentDto.builder()
                .id(comment.getId())
                .bookId(bookId)
                .memberId(memberId)
//                .nickname(member.getNickname())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .replyToCommentId(replyToCommentId)
                .build();
    }
 
    /**
     * 답글 작성
     * - 댓글 ID로 부모 댓글 조회 후 답글 저장
     * - 답글의 답글 방지 (1depth만 허용)
     */
    @Override
    public CommentDto addReply(Long memberId, Long commentId, String content) throws Exception {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));
 
        Comment parentComment = commentRepository.findByIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new CustomException(FriendLibraryErrorCode.COMMENT_NOT_FOUND));
 
        // 답글의 답글 방지 (1depth만 허용)
        if (parentComment.getReplyTo() != null) {
            throw new CustomException(FriendLibraryErrorCode.REPLY_DEPTH_EXCEEDED);
        }
 
        Comment reply = commentRepository.save(Comment.builder()
                .member(member)
                .book(parentComment.getBook())
                .content(content)
                .replyTo(parentComment)
                .isDeleted(false)
                .build());
 
        // book의 댓글 수 증가
        parentComment.getBook().setCommentCount(parentComment.getBook().getCommentCount() + 1);
 
        return CommentDto.builder()
                .id(reply.getId())
                .bookId(parentComment.getBook().getId())
                .memberId(memberId)
//                .nickname(member.getNickname())
                .content(reply.getContent())
                .createdAt(reply.getCreatedAt())
                .replyToCommentId(commentId)
                .build();
    }
 
    /**
     * 댓글 수정
     * - 작성자만 수정 가능
     * - 삭제된 댓글은 수정 불가
     * - updatedAt은 컨트롤러에서 처리
     */
    @Override
    public void updateComment(Long memberId, Long commentId, String content) throws Exception {
        Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new CustomException(FriendLibraryErrorCode.COMMENT_NOT_FOUND));
 
        // 작성자 검증
        if (!comment.getMember().getId().equals(memberId)) {
            throw new CustomException(CommonErrorCode.FORBIDDEN);
        }
 
        comment.setContent(content);
    }
 
    /**
     * 댓글 삭제 (소프트 딜리트)
     * - 작성자만 삭제 가능
     * - is_deleted = true로 처리, 실제 row는 유지
     */
    @Override
    public void deleteComment(Long memberId, Long commentId) throws Exception {
        Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new CustomException(FriendLibraryErrorCode.COMMENT_NOT_FOUND));
 
        // 작성자 검증
        if (!comment.getMember().getId().equals(memberId)) {
            throw new CustomException(CommonErrorCode.FORBIDDEN);
        }
 
        // 소프트 딜리트
        comment.setIsDeleted(true);
 
        // book의 댓글 수 감소 (0 이하로 내려가지 않도록)
        comment.getBook().setCommentCount(Math.max(0, comment.getBook().getCommentCount() - 1));
    }
}