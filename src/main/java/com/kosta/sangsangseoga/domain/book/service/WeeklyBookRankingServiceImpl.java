package com.kosta.sangsangseoga.domain.book.service;

import com.kosta.sangsangseoga.domain.book.dto.WeeklyBookRankingDto;
import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.book.entity.WeeklyBookRanking;
import com.kosta.sangsangseoga.domain.book.repository.BookRepository;
import com.kosta.sangsangseoga.domain.book.repository.WeeklyBookRankingRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeeklyBookRankingServiceImpl implements WeeklyBookRankingService {

    private final WeeklyBookRankingRepository weeklyBookRankingRepository;
    private final BookRepository bookRepository;

    /**
     * 주간 인기 TOP5 조회
     * - weekly_book_ranking 테이블에서 이번 주 score 내림차순 상위 5개
     */
    @Override
    public WeeklyBookRankingDto getWeeklyRanking() throws Exception {
        // 이번 주 월요일 계산
        LocalDate weekStartDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        List<WeeklyBookRanking> rankings = weeklyBookRankingRepository
                .findTop5ByWeekStartDateOrderByScoreDesc(weekStartDate);

        List<WeeklyBookRankingDto.RankItem> items = new ArrayList<>();
        for (int i = 0; i < rankings.size(); i++) {
            WeeklyBookRanking ranking = rankings.get(i);
            Book book = ranking.getBook();

            items.add(WeeklyBookRankingDto.RankItem.builder()
                    .rankNum(i + 1)
                    .bookId(book.getId())
                    .title(book.getTitle())
//                    .authorNickname(book.getMember().getNickname())
                    .coverImageId(book.getCoverImageId())
                    .viewCount(book.getViewCount())
                    .likeCount(book.getLikeCount())
                    .score(ranking.getScore())
                    .build());
        }

        return WeeklyBookRankingDto.builder()
                .weekStartDate(weekStartDate)
                .items(items)
                .build();
    }

    /**
     * 이번 주 신작 TOP5 조회
     * - book 테이블에서 이번 주 월요일 이후 등록된 책 중 score 기준 상위 5개
     * - 동점 시 등록일 최신순
     * - 신작이 5개 미만이면 있는 만큼만 반환
     */
    @Override
    public WeeklyBookRankingDto getWeeklyNewReleases() throws Exception {
        // 이번 주 월요일 00시 계산
        LocalDate weekStartDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime weekStartDateTime = weekStartDate.atStartOfDay();

        List<Book> books = bookRepository.findTop5NewReleases(weekStartDateTime);

        List<WeeklyBookRankingDto.RankItem> items = new ArrayList<>();
        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            int score = book.getViewCount() * 1 + book.getLikeCount() * 3;

            items.add(WeeklyBookRankingDto.RankItem.builder()
                    .rankNum(i + 1)
                    .bookId(book.getId())
                    .title(book.getTitle())
//                    .authorNickname(book.getMember().getNickname())
                    .coverImageId(book.getCoverImageId())
                    .viewCount(book.getViewCount())
                    .likeCount(book.getLikeCount())
                    .score(score)
                    .build());
        }

        return WeeklyBookRankingDto.builder()
                .weekStartDate(weekStartDate)
                .items(items)
                .build();
    }
}