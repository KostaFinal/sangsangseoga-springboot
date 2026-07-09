package com.kosta.sangsangseoga.domain.book.entity;

import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.book.enums.AgeGroup;
import com.kosta.sangsangseoga.domain.book.enums.BookStatus;
import com.kosta.sangsangseoga.domain.book.enums.BookType;
import com.kosta.sangsangseoga.domain.book.enums.CreationMode;
import com.kosta.sangsangseoga.global.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "book")
public class Book extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookType bookType;

    @Enumerated(EnumType.STRING)
    private CreationMode creationMode;

    @Enumerated(EnumType.STRING)
    private AgeGroup authorAgeGroup;

    @Enumerated(EnumType.STRING)
    private AgeGroup readerAgeGroup;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String category;
    private String targetLang;
    private String styleCode;

    @Column(name = "cover_image_id", unique = true)
    private Long coverImageId;

    @Lob
    private String confirmedSettings;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private BookStatus status = BookStatus.PUBLISHED;

    @Column(nullable = false)
    private Integer pageCount;

    @Column(nullable = false)
    private Integer viewCount;

    @Column(nullable = false)
    private Integer likeCount;

    @Column(nullable = false)
    private Integer commentCount;

    // 이번 주(월요일) 랭킹 집계 시점의 누적 조회수/좋아요 스냅샷 - 다음 주 집계 때 "이번 주 증가분"을 구하는 기준점.
    // 기존 행에는 값이 없을 수 있어 nullable로 두고, 사용하는 쪽에서 null을 0으로 취급한다.
    private Integer weekStartViewCount;
    private Integer weekStartLikeCount;
}