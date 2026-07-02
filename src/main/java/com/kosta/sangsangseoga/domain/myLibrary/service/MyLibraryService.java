package com.kosta.sangsangseoga.domain.myLibrary.service;

import java.util.List;

import com.kosta.sangsangseoga.domain.myLibrary.dto.FinishedBookResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.ReadingBookResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.ReadingProgressRequestDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.WishlistBookResponseDto;

public interface MyLibraryService {
	
	//읽고 싶은 책 목록 조회
	List<WishlistBookResponseDto> getWishlist(Long memberId) throws Exception;
	
	//읽고 싶은 책 삭제
	void deleteWishlist(Long memberId, Long bookId) throws Exception;
	
	List<ReadingBookResponseDto> getReadingList(Long memberId) throws Exception;
	
	List<FinishedBookResponseDto> getFinishedList(Long memberId) throws Exception;
	
	void updateReadingProgress(Long memberId, Long bookId, ReadingProgressRequestDto readingProgressRequestDto) throws Exception;
	
}
