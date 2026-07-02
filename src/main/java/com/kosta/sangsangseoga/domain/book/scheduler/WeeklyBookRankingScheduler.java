package com.kosta.sangsangseoga.domain.book.scheduler;

import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.book.entity.WeeklyBookRanking;
import com.kosta.sangsangseoga.domain.book.repository.BookRepository;
import com.kosta.sangsangseoga.domain.book.repository.WeeklyBookRankingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyBookRankingScheduler {

    private final WeeklyBookRankingRepository weeklyBookRankingRepository;
    private final BookRepository bookRepository;

    /**
     * 주간 인기 책 TOP5 집계
     * - 매주 월요일 00:00에 실행
     * - 전체 PUBLISHED 책 중 score(조회수×1 + 좋아요×3) 기준 상위 5개 저장
     */
    @Scheduled(cron = "0 0 0 * * MON")
    @Transactional
    public void aggregateWeeklyRanking() {
        log.info("주간 인기 책 TOP5 집계 시작");

        LocalDate weekStartDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // 이번 주 기존 데이터 전체 삭제 후 재집계
        weeklyBookRankingRepository.deleteByWeekStartDate(weekStartDate);

        // 전체 PUBLISHED 책 중 score 기준 상위 5개 조회 후 저장
        List<Book> books = bookRepository.findTop5ForWeeklyRanking(PageRequest.of(0, 5));
        for (Book book : books) {
            int score = book.getViewCount() * 1 + book.getLikeCount() * 3;
            weeklyBookRankingRepository.save(WeeklyBookRanking.builder()
                    .book(book)
                    .weekStartDate(weekStartDate)
                    .score(score)
                    .build());
        }

        log.info("주간 인기 책 TOP5 집계 완료 - {}건", books.size());
    }
}