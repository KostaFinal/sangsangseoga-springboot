package com.kosta.sangsangseoga.domain.myLibrary.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.myLibrary.dto.CategoryStatsDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.FinishedBookResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.ReadingBookResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.ReadingProgressRequestDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.ReadingStatsResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.WishlistBookResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.entity.MyReading;
import com.kosta.sangsangseoga.domain.myLibrary.enums.ReadingStatus;
import com.kosta.sangsangseoga.domain.myLibrary.exception.ReadingErrorCode;
import com.kosta.sangsangseoga.domain.myLibrary.repository.BookReviewRepository;
import com.kosta.sangsangseoga.domain.myLibrary.repository.MyReadingRepository;
import com.kosta.sangsangseoga.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
@Transactional
public class MyLibraryServiceImpl implements MyLibraryService {
	
	private final MyReadingRepository myReadingRepository;
	private final BookReviewRepository bookReviewRepository;

	@Override
	@Transactional(readOnly = true)
	public List<WishlistBookResponseDto> getWishlist(Long memberId){
		return myReadingRepository
				.findByMember_IdAndReadingStatus(memberId, ReadingStatus.WISH)
				.stream()
				.map(myReading -> {
					Book book = myReading.getBook();
					
					return WishlistBookResponseDto.builder()
							.bookId(book.getId())
							.title(book.getTitle())
							.description(book.getDescription())
							.category(book.getCategory())
							.build();
				})
				.collect(Collectors.toList());
	}

	@Override
	public void deleteWishlist(Long memberId, Long bookId) {
		MyReading myReading = myReadingRepository
	            .findByMember_IdAndBook_IdAndReadingStatus(
	                    memberId,
	                    bookId,
	                    ReadingStatus.WISH
	            )
	            .orElseThrow(() -> new CustomException(ReadingErrorCode.WISHLIST_NOT_FOUND));

	    myReadingRepository.delete(myReading);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ReadingBookResponseDto> getReadingList(Long memberId)  {
		return myReadingRepository
				.findByMember_IdAndReadingStatus(memberId, ReadingStatus.READING)
				.stream()
				.map(myReading -> {
					Book book = myReading.getBook();
					
					return ReadingBookResponseDto.builder()
							.bookId(book.getId())
							.title(book.getTitle())
							.category(book.getCategory())
							.currentPage(myReading.getCurrentPage())
							.progress(myReading.getProgress())
							.pageCount(book.getPageCount())
							.build();
				})
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public List<FinishedBookResponseDto> getFinishedList(Long memberId)  {
		 return myReadingRepository
		            .findByMember_IdAndReadingStatus(memberId, ReadingStatus.COMPLETED)
		            .stream()
		            .map(myReading -> {
		                Book book = myReading.getBook();

		                return FinishedBookResponseDto.builder()
		                        .bookId(book.getId())
		                        .title(book.getTitle())
		                        .category(book.getCategory())
		                        .completedAt(myReading.getCompletedAt())
		                        .readingTime(myReading.getReadingTime())
		                        .build();
		            })
		            .collect(Collectors.toList());
	}

	@Override
	public void updateReadingProgress(Long memberId, Long bookId, ReadingProgressRequestDto readingProgressRequestDto){
		 MyReading myReading = myReadingRepository
		            .findByMember_IdAndBook_IdAndReadingStatus(memberId, bookId, ReadingStatus.READING)
		            .orElseThrow(() -> new CustomException(ReadingErrorCode.MY_READING_NOT_FOUND));

		    myReading.setCurrentPage(readingProgressRequestDto.getCurrentPage());
		    myReading.setProgress(readingProgressRequestDto.getProgress());
		    myReading.setRecentReadAt(LocalDateTime.now());
	}

	@Override
	public void completeReading(Long memberId, Long bookId){
		 MyReading myReading = myReadingRepository
		            .findByMember_IdAndBook_Id(memberId, bookId)
		            .orElseThrow(() -> new CustomException(ReadingErrorCode.MY_READING_NOT_FOUND));

		    myReading.setReadingStatus(ReadingStatus.COMPLETED);
		    myReading.setProgress(100);
		    myReading.setCompletedAt(LocalDateTime.now());
		    myReading.setRecentReadAt(LocalDateTime.now());
		
	}

	@Override
	public void rereadBook(Long memberId, Long bookId){
		 MyReading myReading = myReadingRepository
		            .findByMember_IdAndBook_Id(memberId, bookId)
		            .orElseThrow(() -> new CustomException(ReadingErrorCode.MY_READING_NOT_FOUND));

		    myReading.setReadingStatus(ReadingStatus.READING);
		    myReading.setCurrentPage(1);
		    myReading.setProgress(0);
		    myReading.setCompletedAt(null);
		    myReading.setRecentReadAt(LocalDateTime.now());
		
	}

	@Override
	@Transactional(readOnly = true)
	public ReadingBookResponseDto getLastReadingPosition(Long memberId, Long bookId) {
		MyReading myReading = myReadingRepository
	            .findByMember_IdAndBook_Id(memberId, bookId)
	            .orElseThrow(() -> new CustomException(ReadingErrorCode.MY_READING_NOT_FOUND));

	    Book book = myReading.getBook();

	    return ReadingBookResponseDto.builder()
	            .bookId(book.getId())
	            .title(book.getTitle())
	            .category(book.getCategory())
	            .currentPage(myReading.getCurrentPage())
	            .progress(myReading.getProgress())
	            .pageCount(book.getPageCount())
	            .build();
	}

	@Override
	@Transactional(readOnly = true)
	public ReadingStatsResponseDto getReadingStats(Long memberId){
		Long wishlistBookCount = myReadingRepository
	            .countByMember_IdAndReadingStatus(memberId, ReadingStatus.WISH);

	    Long readingBookCount = myReadingRepository
	            .countByMember_IdAndReadingStatus(memberId, ReadingStatus.READING);

	    Long completedBookCount = myReadingRepository
	            .countByMember_IdAndReadingStatus(memberId, ReadingStatus.COMPLETED);

	    Long totalReadingTime = myReadingRepository.sumReadingTimeByMemberId(memberId);
	    
	    Long reportCount = bookReviewRepository.countByMember_Id(memberId);

	    List<MyReading> myReadings = myReadingRepository.findByMember_Id(memberId);

	    Long totalPagesRead = myReadings.stream()
	            .mapToLong(myReading -> {
	                Book book = myReading.getBook();

	                int pageCount = book.getPageCount() != null ? book.getPageCount() : 0;
	                int progress = myReading.getProgress() != null ? myReading.getProgress() : 0;

	                return Math.round(pageCount * (progress / 100.0));
	            })
	            .sum();

	    List<CategoryStatsDto> categoryStats = myReadings.stream()
	            .collect(Collectors.groupingBy(
	                    myReading -> myReading.getBook().getCategory() != null
	                            ? myReading.getBook().getCategory()
	                            : "기타",
	                    Collectors.counting()
	            ))
	            .entrySet()
	            .stream()
	            .map(entry -> CategoryStatsDto.builder()
	                    .category(entry.getKey())
	                    .count(entry.getValue())
	                    .build())
	            .collect(Collectors.toList());

	    List<FinishedBookResponseDto> finishedBooks = getFinishedList(memberId);

	    return ReadingStatsResponseDto.builder()
	            .totalReadingTime(totalReadingTime)
	            .totalPagesRead(totalPagesRead)
	            .reportCount(reportCount)
	            .wishlistBookCount(wishlistBookCount)
	            .readingBookCount(readingBookCount)
	            .completedBookCount(completedBookCount)
	            .categoryStats(categoryStats)
	            .finishedBooks(finishedBooks)
	            .build();
	}

}
