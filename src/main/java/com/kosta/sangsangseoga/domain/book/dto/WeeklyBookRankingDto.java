package com.kosta.sangsangseoga.domain.book.dto;
 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.time.LocalDate;
import java.util.List;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyBookRankingDto {
 
    private List<RankItem> items;
    private LocalDate weekStartDate;
 
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RankItem {
 
        private Integer rankNum;
        private Long bookId;
        private String title;
        private String authorNickname;
        private String coverImageUrl;
        private String bookType;
        private Integer viewCount;
        private Integer likeCount;
        private Integer score;
    }
}