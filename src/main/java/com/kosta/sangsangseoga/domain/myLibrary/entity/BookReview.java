package com.kosta.sangsangseoga.domain.myLibrary.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.kosta.sangsangseoga.domain.account.entity.Member;
import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.global.common.BaseEntity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "book_review")
public class BookReview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    //작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id", nullable = false)
    private Member member;
    
    //책
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    
    //독후감 내용
    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;
    
    //임시저장 여부
    @Column(name = "is_draft")
    private Boolean isDraft;
    
    //AI 피드백
    @Column(name = "ai_feedback_content", columnDefinition = "TEXT")
    private String aiFeedbackContent;
    
    //AI 피드백 생성일
    @Column(name = "ai_feedback_created_at")
    private LocalDateTime aiFeedbackCreatedAt;
}
