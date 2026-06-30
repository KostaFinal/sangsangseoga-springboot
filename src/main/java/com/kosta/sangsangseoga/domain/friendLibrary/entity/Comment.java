package com.kosta.sangsangseoga.domain.friendLibrary.entity;

import com.kosta.sangsangseoga.domain.account.entity.Member;
import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.global.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "comment")
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    // 탈퇴 시 NULL로 익명화하기 위해 nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 답글 대상 댓글. 일반 댓글이면 NULL. 1depth만 허용 (replyTo 자체가 reply면 안 됨 - 서비스단에서 검증)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    private Comment replyTo;

    @Column(length = 255, nullable = false)
    private String content;

    @Column(nullable = false)
    private Boolean isDeleted;
}