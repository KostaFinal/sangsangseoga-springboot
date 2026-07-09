package com.kosta.sangsangseoga.domain.myLibrary.service;

import java.util.List;

import com.kosta.sangsangseoga.domain.myLibrary.dto.FinishedBookResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.MyWrittenBookResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.ReadingBookResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.ReadingProgressRequestDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.ReadingStatsResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.UpdateBookDescriptionRequestDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.UpdateBookStatusRequestDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.WishlistBookResponseDto;

public interface MyLibraryService {
	
	//읽고 싶은 책 목록 조회
	List<WishlistBookResponseDto> getWishlist(Long memberId);
	
	void addWishlist(Long memberId, Long bookId);
	
	//읽고 싶은 책 삭제
	void deleteWishlist(Long memberId, Long bookId);
	
	List<ReadingBookResponseDto> getReadingList(Long memberId);
	
	List<FinishedBookResponseDto> getFinishedList(Long memberId);
	
	void updateReadingProgress(Long memberId, Long bookId, ReadingProgressRequestDto readingProgressRequestDto);
	
	// 완독 처리
	void completeReading(Long memberId, Long bookId);

	// 다시 읽기
	void rereadBook(Long memberId, Long bookId);
	
	// 마지막 읽은 위치 조회
	ReadingBookResponseDto getLastReadingPosition(Long memberId, Long bookId);
	
	//독서 통계 조회
	ReadingStatsResponseDto getReadingStats(Long memberId);
	
	List<MyWrittenBookResponseDto> getMyWrittenBooks(Long memberId);
	
	void updateMyWrittenBookStatus(Long memberId, Long bookId, UpdateBookStatusRequestDto requestDto);
	
	void updateMyWrittenBookDescription(Long memberId, Long bookId, UpdateBookDescriptionRequestDto requestDto);
}
