package com.kosta.sangsangseoga.domain.book.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "weekly_book_stat")
public class WeeklyBookRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
