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
     * - 전체 PUBLISHED 책 중 이번 주 증가분(현재 누적 - 지난주 기준점) 기준 score(조회수×1 + 좋아요×3) 상위 5개 저장
     * - 집계 후 전체 PUBLISHED 책의 기준점을 현재 누적치로 갱신해 다음 주 증가분 계산에 사용
     */
    @Scheduled(cron = "0 0 0 * * MON")
    @Transactional
    public void aggregateWeeklyRanking() {
        log.info("주간 인기 책 TOP5 집계 시작");

        LocalDate weekStartDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // 이번 주 기존 데이터 전체 삭제 후 재집계
        weeklyBookRankingRepository.deleteByWeekStartDate(weekStartDate);

        // 전체 PUBLISHED 책 중 이번 주 증가분 기준 score 상위 5개 조회 후 저장
        List<Book> books = bookRepository.findTop5ForWeeklyRanking(PageRequest.of(0, 5));
        for (Book book : books) {
            int weekViewCount = book.getViewCount() - (book.getWeekStartViewCount() != null ? book.getWeekStartViewCount() : 0);
            int weekLikeCount = book.getLikeCount() - (book.getWeekStartLikeCount() != null ? book.getWeekStartLikeCount() : 0);
            int score = weekViewCount * 1 + weekLikeCount * 3;
            weeklyBookRankingRepository.save(WeeklyBookRanking.builder()
                    .book(book)
                    .weekStartDate(weekStartDate)
                    .score(score)
                    .build());
        }

        // 다음 주 증가분 계산의 기준점을 현재 누적치로 갱신 (TOP5 안에 없던 책들도 포함, 전체 대상)
        bookRepository.resetWeeklyBaseline();

        log.info("주간 인기 책 TOP5 집계 완료 - {}건", books.size());
    }
}