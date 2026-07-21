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

    // 이번 주 조회수/좋아요 - 조회/좋아요가 발생할 때마다 실시간으로 증가(좋아요 취소 시 감소)하고,
    // 매주 월요일 랭킹 집계 후 0으로 초기화된다. DB에서 바로 "이번 주 수치"를 확인할 수 있다.
    // (좋아요 취소는 음수가 될 수 있음 - 저번 주 좋아요를 이번 주에 취소한 경우)
    @Column(nullable = false)
    @Builder.Default
    private Integer weekViewCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer weekLikeCount = 0;
}