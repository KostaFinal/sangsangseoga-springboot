package com.kosta.sangsangseoga.domain.book.repository;
 
import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.book.entity.BookPage;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
 
public interface BookPageRepository extends JpaRepository<BookPage, Long> {
 
    // 책의 전체 페이지 목록 pageNo 오름차순 조회
    List<BookPage> findByBookOrderByPageNoAsc(Book book);
}