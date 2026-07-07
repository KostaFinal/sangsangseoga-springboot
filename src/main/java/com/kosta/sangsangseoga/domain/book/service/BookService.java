package com.kosta.sangsangseoga.domain.book.service;
 
import com.kosta.sangsangseoga.domain.book.dto.BookContentsResponseDto;
import com.kosta.sangsangseoga.domain.book.dto.BookDetailDto;
import com.kosta.sangsangseoga.domain.book.dto.BookListResponseDto;
import com.kosta.sangsangseoga.domain.book.dto.BookRecommendResponseDto;
 
public interface BookService {

    // TODO: "책 생성" 메서드 추가 시 UsageService와 연동 필요.
    //  - 시작 시 UsageService.canStartFreeTrial() 체크 -> 통과하면 markFreeTrialUsed() 호출(생애 1회 소진)
    //  - 페이지 추가 시 UsageService.canAddFreeTrialPage()로 10페이지 제한 체크(FREE 회원 한정)

    // 책 목록 조회 (장르 필터/정렬/검색/페이징)
    BookListResponseDto getBooks(String genre, String sort, String keyword, int page, int size, Long memberId) throws Exception;
 
    // 책 목록 조회 (bookType 필터/정렬/검색/페이징)
    BookListResponseDto getBooks(String bookType, String sort, String keyword, int page, int size, Long memberId) throws Exception;
    
 // 책 상세 조회
    BookDetailDto getBook(Long bookId, Long memberId) throws Exception;
    
 // 책 본문(페이지) 조회
    BookContentsResponseDto getContents(Long bookId) throws Exception;
    
 // 함께 읽기 좋은 작품 추천
    BookRecommendResponseDto getRecommendations(Long bookId, int size) throws Exception;
}