package com.kosta.sangsangseoga.domain.myLibrary.entity;
 
import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.global.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import javax.persistence.*;
import java.math.BigDecimal;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "reading_memo", uniqueConstraints = {
        @UniqueConstraint(name = "uk_reading_memo_member_book_page", columnNames = {"member_id", "book_id", "page_no"})
})
public class ReadingMemo extends BaseEntity {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
 
    @Column(name = "page_no", nullable = false)
    private Integer pageNo;
 
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
 
    // �޸� ��ġ X ��ǥ
    private BigDecimal posX;
 
    // �޸� ��ġ Y ��ǥ
    private BigDecimal posY;
}