package com.kosta.sangsangseoga.domain.book.repository;
 
import com.kosta.sangsangseoga.domain.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.time.LocalDateTime;
import java.util.List;
 
public interface BookRepository extends JpaRepository<Book, Long> {
 
    // 이번 주 등록된 책 중 score(조회수×1 + 좋아요×3) 기준 상위 5개 조회
    // 동점 시 등록일 최신순
    @Query("SELECT b FROM Book b WHERE b.createdAt >= :weekStart AND b.status = 'PUBLISHED' " +
           "ORDER BY (b.viewCount * 1 + b.likeCount * 3) DESC, b.createdAt DESC")
    List<Book> findTop5NewReleases(@Param("weekStart") LocalDateTime weekStart);
}
 