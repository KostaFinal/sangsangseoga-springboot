package com.kosta.sangsangseoga.domain.book.entity;

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
@Table(name = "book_page")
public class BookPage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private Integer pageNo;

    private String title;

    private String titleEn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentType contentType;

    @Column(columnDefinition = "TEXT")
    private String contentTextKo;

    @Column(columnDefinition = "TEXT")
    private String contentTextEn;

    // 영어 번역이 기본 글자 크기(17px) 박스에 안 들어갈 때, 잘라내는 대신 이 페이지의
    // 영어 렌더링만 줄인 글자 크기(px). null이면 리더가 기본 크기를 쓴다.
    private Integer contentFontSizeEn;

    private String imageUrl;

    public enum ContentType {
        PAGE, SCENE, CHAPTER, POEM, ESSAY
    }
}