package com.kosta.sangsangseoga.domain.book.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
 
import javax.persistence.*;
import java.time.LocalDate;
 
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "weekly_book_ranking")
public class WeeklyBookRanking {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
 
    @Column(nullable = false)
    private LocalDate weekStartDate;
 
    @Column(nullable = false)
    private Integer score;
}
 