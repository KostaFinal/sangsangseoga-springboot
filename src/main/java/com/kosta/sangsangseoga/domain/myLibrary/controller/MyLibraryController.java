package com.kosta.sangsangseoga.domain.myLibrary.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kosta.sangsangseoga.domain.myLibrary.dto.FinishedBookResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.ReadingBookResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.ReadingProgressRequestDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.WishlistBookResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.service.MyLibraryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bookshelves")
@RequiredArgsConstructor
public class MyLibraryController {
	
	private final MyLibraryService myLibraryService;

    // 읽고 싶은 책 조회
    @GetMapping("/wishlist")
    public ResponseEntity<List<WishlistBookResponseDto>> getWishlist(
            @RequestParam Long memberId) throws Exception {

        return ResponseEntity.ok(myLibraryService.getWishlist(memberId));
    }

    // 읽고 싶은 책 삭제
    @DeleteMapping("/wishlist/{bookId}")
    public ResponseEntity<Void> deleteWishlist(
            @RequestParam Long memberId,
            @PathVariable Long bookId) throws Exception {

        myLibraryService.deleteWishlist(memberId, bookId);
        return ResponseEntity.noContent().build();
    }

    // 읽는 중 목록 조회
    @GetMapping("/reading")
    public ResponseEntity<List<ReadingBookResponseDto>> getReadingList(
            @RequestParam Long memberId) throws Exception {

        return ResponseEntity.ok(myLibraryService.getReadingList(memberId));
    }

    // 읽기 완료 목록 조회
    @GetMapping("/finished")
    public ResponseEntity<List<FinishedBookResponseDto>> getFinishedList(
            @RequestParam Long memberId) throws Exception {

        return ResponseEntity.ok(myLibraryService.getFinishedList(memberId));
    }

    // 독서 진행률 저장
    @PatchMapping("/reading/{bookId}/progress")
    public ResponseEntity<Void> updateReadingProgress(
            @RequestParam Long memberId,
            @PathVariable Long bookId,
            @RequestBody ReadingProgressRequestDto requestDto) throws Exception {

        myLibraryService.updateReadingProgress(memberId, bookId, requestDto);
        return ResponseEntity.ok().build();
    }
}
