package com.kosta.sangsangseoga.domain.myLibrary.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kosta.sangsangseoga.domain.myLibrary.dto.FinishedBookResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.ReadingBookResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.ReadingProgressRequestDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.ReadingStatsResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.WishlistBookResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.service.MyLibraryService;
import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bookshelves")
@RequiredArgsConstructor
public class MyLibraryController {

	private final MyLibraryService myLibraryService;
	
	private Long getMemberId(Authentication authentication) {
	    if (authentication == null || authentication.getPrincipal() == null) {
	        throw new CustomException(CommonErrorCode.UNAUTHORIZED);
	    }

	    return (Long) authentication.getPrincipal();
	}
	

	// 읽고 싶은 책 조회
	@GetMapping("/wishlist")
	public ResponseEntity<List<WishlistBookResponseDto>> getWishlist(Authentication authentication)  {
		Long memberId = getMemberId(authentication);
	    return ResponseEntity.ok(myLibraryService.getWishlist(memberId));
	}
	
	//읽고 싶은 책 등록
	@PostMapping("/wishlist/{bookId}")
	public ResponseEntity<Void> addWishlist(
	        Authentication authentication,
	        @PathVariable Long bookId
	) {
	    Long memberId = getMemberId(authentication);
	    myLibraryService.addWishlist(memberId, bookId);
	    return ResponseEntity.ok().build();
	}

	// 읽고 싶은 책 삭제
	@DeleteMapping("/wishlist/{bookId}")
	public ResponseEntity<Void> deleteWishlist(Authentication authentication, @PathVariable Long bookId){

		Long memberId = getMemberId(authentication);

	    myLibraryService.deleteWishlist(memberId, bookId);
	    return ResponseEntity.noContent().build();
	}

	// 읽는 중 목록 조회
	@GetMapping("/reading")
	public ResponseEntity<List<ReadingBookResponseDto>> getReadingList(
	        Authentication authentication)  {

		Long memberId = getMemberId(authentication);
	    return ResponseEntity.ok(myLibraryService.getReadingList(memberId));
	}

	// 읽기 완료 목록 조회
	@GetMapping("/finished")
	public ResponseEntity<List<FinishedBookResponseDto>> getFinishedList(
	        Authentication authentication) {

		Long memberId = getMemberId(authentication);
	    return ResponseEntity.ok(myLibraryService.getFinishedList(memberId));
	}

	// 독서 진행률 저장
	@PatchMapping("/reading/{bookId}/progress")
	public ResponseEntity<Void> updateReadingProgress(
	        Authentication authentication,
	        @PathVariable Long bookId,
	        @RequestBody ReadingProgressRequestDto requestDto) {

		Long memberId = getMemberId(authentication);

	    myLibraryService.updateReadingProgress(memberId, bookId, requestDto);
	    return ResponseEntity.ok().build();
	}

	// 완독 처리
	@PatchMapping("/reading/{bookId}/complete")
	public ResponseEntity<Void> completeReading(Authentication authentication, @PathVariable Long bookId){

		Long memberId = getMemberId(authentication);
		
		myLibraryService.completeReading(memberId, bookId);
		return ResponseEntity.ok().build();
	}

	// 다시 읽기
	@PatchMapping("/finished/{bookId}/reread")
	public ResponseEntity<Void> rereadBook(Authentication authentication, @PathVariable Long bookId){

		Long memberId = getMemberId(authentication);
		myLibraryService.rereadBook(memberId, bookId);
		return ResponseEntity.ok().build();
	}

	// 마지막 읽은 위치 조회
	@GetMapping("/reading/{bookId}/last-position")
	public ResponseEntity<ReadingBookResponseDto> getLastReadingPosition(Authentication authentication,
			@PathVariable Long bookId){

		Long memberId = getMemberId(authentication);
		return ResponseEntity.ok(myLibraryService.getLastReadingPosition(memberId, bookId));
	}

	// 독서 통계 조회
	@GetMapping("/stats")
	public ResponseEntity<ReadingStatsResponseDto> getReadingStats(Authentication authentication){

		Long memberId = getMemberId(authentication);

		return ResponseEntity.ok(myLibraryService.getReadingStats(memberId));
	}
}
