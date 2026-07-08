package com.kosta.sangsangseoga.domain.book.service;
 
import com.kosta.sangsangseoga.domain.book.dto.BookContentsResponseDto;
import com.kosta.sangsangseoga.domain.book.dto.BookDetailDto;
import com.kosta.sangsangseoga.domain.book.dto.BookListResponseDto;
import com.kosta.sangsangseoga.domain.book.dto.BookRecommendResponseDto;
 
public interface BookService {
 
    // 책 목록 조회 (장르 필터/정렬/검색/페이징)
    BookListResponseDto getBooks(String genre, String sort, String keyword, int page, int size, Long memberId) throws Exception;
    
 // 책 상세 조회
    BookDetailDto getBook(Long bookId, Long memberId) throws Exception;
    
 // 책 본문(페이지) 조회
    BookContentsResponseDto getContents(Long bookId) throws Exception;
    
 // 함께 읽기 좋은 작품 추천
    BookRecommendResponseDto getRecommendations(Long bookId, int size) throws Exception;
    
 // 내가 쓴 책 목록 조회
    BookListResponseDto getMyBooks(Long memberId) throws Exception;
}