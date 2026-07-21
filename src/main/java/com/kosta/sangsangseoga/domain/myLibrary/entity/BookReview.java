package com.kosta.sangsangseoga.domain.myLibrary.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.global.common.BaseEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "book_review")
public class BookReview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id", nullable = false)
    private Member member;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    
    @Lob
    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;
    
    @Column(name = "is_draft", nullable = false)
    private Boolean isDraft = false;
    
    @Column(name = "ai_feedback_content", columnDefinition = "TEXT")
    private String aiFeedbackContent;
    
    @Column(name = "ai_feedback_created_at")
    private LocalDateTime aiFeedbackCreatedAt;
    

    public void publish() {
        this.isDraft = false;
    }

    public void updateAiFeedback(String aiFeedbackContent) {
        this.aiFeedbackContent = aiFeedbackContent;
        this.aiFeedbackCreatedAt = LocalDateTime.now();
    }
    
    
}
