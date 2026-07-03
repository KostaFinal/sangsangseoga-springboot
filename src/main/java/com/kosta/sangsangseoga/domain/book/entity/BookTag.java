package com.kosta.sangsangseoga.domain.book.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "book_tag", uniqueConstraints = {
        @UniqueConstraint(name = "uk_book_tag_book_tag_name", columnNames = {"book_id", "tag_name"})
})
public class BookTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "tag_name", length = 50, nullable = false)
    private String tagName;

    @Builder
    private BookTag(Book book, String tagName) {
        this.book = book;
        this.tagName = tagName;
    }
}
