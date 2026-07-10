package com.kosta.sangsangseoga.domain.book.service;
 
import com.kosta.sangsangseoga.domain.book.dto.BookContentsResponseDto;
import com.kosta.sangsangseoga.domain.book.dto.BookDetailDto;
import com.kosta.sangsangseoga.domain.book.dto.BookListResponseDto;
import com.kosta.sangsangseoga.domain.book.dto.BookPublishRequestDto;
import com.kosta.sangsangseoga.domain.book.dto.BookPublishResponseDto;
import com.kosta.sangsangseoga.domain.book.dto.BookRecommendResponseDto;

public interface BookService {

    // 책 생성(최종 저장). FREE 회원은 UsageService의 생애 1회 체험/10페이지 제한을 통과해야 한다.
    BookPublishResponseDto publish(Long memberId, BookPublishRequestDto request);

    // 책 목록 조회 (bookType 필터/정렬/검색/페이징)
    BookListResponseDto getBooks(String bookType, String sort, String keyword, int page, int size, Long memberId) throws Exception;
    
 // 책 상세 조회
    BookDetailDto getBook(Long bookId, Long memberId) throws Exception;

 // 책 읽기 시작 시 조회수 증가
    Integer increaseViewCount(Long bookId) throws Exception;

 // 책 본문(페이지) 조회
    BookContentsResponseDto getContents(Long bookId) throws Exception;
    
 // 함께 읽기 좋은 작품 추천
    BookRecommendResponseDto getRecommendations(Long bookId, int size) throws Exception;
    
 // 내가 쓴 책 목록 조회
    BookListResponseDto getMyBooks(Long memberId) throws Exception;
}