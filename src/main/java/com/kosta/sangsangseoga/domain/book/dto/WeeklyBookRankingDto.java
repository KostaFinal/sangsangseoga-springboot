package com.kosta.sangsangseoga.domain.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.time.LocalDate;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyBookRankingDto {
 
    private Long id;
    private Long bookId;
    private LocalDate weekStartDate;
    private Integer score;
}