package com.kosta.sangsangseoga.domain.book.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "book_image")
public class BookImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    // 본문 삽화일 경우 book_page.id, 표지 이미지는 NULL
    @Column(name = "book_page_id")
    private Long bookPageId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImageType imageType;

    // 한 페이지에 이미지가 여러 개일 경우 순서
    private Integer imageOrder;

    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = false)
    private String fileName;

    private String fileExtension;

    private Long fileSize;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 파일 삭제 시점. 미삭제 시 NULL
    private LocalDateTime deletedAt;

    public enum ImageType {
        COVER, PAGE, CHARACTER, BACKGROUND
    }
}