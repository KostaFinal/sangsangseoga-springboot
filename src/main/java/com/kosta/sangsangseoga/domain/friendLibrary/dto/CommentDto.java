package com.kosta.sangsangseoga.domain.friendLibrary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {

    private Long id;
    private Long bookId;
    private Long memberId;
    private String nickname;
    private String content;
    private LocalDateTime createdAt;
    private Long replyToCommentId; // 일반 댓글이면 null
}