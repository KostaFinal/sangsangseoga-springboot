package com.kosta.sangsangseoga.domain.myLibrary.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.book.entity.BookImage;
import com.kosta.sangsangseoga.domain.book.repository.BookImageRepository;
import com.kosta.sangsangseoga.domain.book.repository.BookRepository;
import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
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
import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
@Transactional
public class MyLibraryServiceImpl implements MyLibraryService {
	
	private final MyReadingRepository myReadingRepository;
	private final BookReviewRepository bookReviewRepository;
	private final BookImageRepository bookImageRepository;
	private final BookRepository bookRepository;
	private final MemberRepository memberRepository;

	@Override
	@Transactional(readOnly = true)
	public List<WishlistBookResponseDto> getWishlist(Long memberId){
		return myReadingRepository
	            .findByMember_IdAndReadingStatus(memberId, ReadingStatus.WISH)
	            .stream()
	            .map(myReading -> {
	                Book book = myReading.getBook();

	                String coverImageUrl = bookImageRepository
	                        .findByBookAndImageTypeAndDeletedAtIsNull(
	                                book,
	                                BookImage.ImageType.COVER
	                        )
	                        .map(BookImage::getFileUrl)
	                        .orElse(null);

	                return WishlistBookResponseDto.builder()
	                        .bookId(book.getId())
	                        .title(book.getTitle())
	                        .description(book.getDescription())
	                        .category(book.getCategory())
	                        .bookType(book.getBookType().name())
	                        .coverImageUrl(coverImageUrl)
	                        .build();
	            })
	            .collect(Collectors.toList());
	}
	
	@Override
	public void addWishlist(Long memberId, Long bookId) {
	    MyReading existing = myReadingRepository
	            .findByMember_IdAndBook_Id(memberId, bookId)
	            .orElse(null);

	    if (existing != null) {
	        existing.setReadingStatus(ReadingStatus.WISH);
	        return;
	    }

	    Member member = memberRepository.findById(memberId)
	            .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

	    Book book = bookRepository.findById(bookId)
	            .orElseThrow(() -> new CustomException(CommonErrorCode.BOOK_NOT_FOUND));

	    MyReading myReading = MyReading.builder()
	            .member(member)
	            .book(book)
	            .readingStatus(ReadingStatus.WISH)
	            .currentPage(1)
	            .progress(0)
	            .rereadCount(0)
	            .readingTime(0)
	            .build();

	    myReadingRepository.save(myReading);
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
				.findByMember_IdAndReadingStatusOrderByRecentReadAtDesc(memberId, ReadingStatus.READING)
	            .stream()
	            .map(myReading -> {
	                Book book = myReading.getBook();

	                String coverImageUrl = bookImageRepository
	                        .findByBookAndImageTypeAndDeletedAtIsNull(
	                                book,
	                                BookImage.ImageType.COVER
	                        )
	                        .map(BookImage::getFileUrl)
	                        .orElse(null);

	                return ReadingBookResponseDto.builder()
	                        .bookId(book.getId())
	                        .title(book.getTitle())
	                        .description(book.getDescription())
	                        .category(book.getCategory())
	                        .bookType(book.getBookType().name())
	                        .coverImageUrl(coverImageUrl)
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
				.findByMember_IdAndRereadCountGreaterThanOrderByCompletedAtDesc(memberId,0)
	            .stream()
	            .map(myReading -> {

	                Book book = myReading.getBook();

	                String coverImageUrl = bookImageRepository
	                        .findByBookAndImageTypeAndDeletedAtIsNull(
	                                book,
	                                BookImage.ImageType.COVER
	                        )
	                        .map(BookImage::getFileUrl)
	                        .orElse(null);

	                return FinishedBookResponseDto.builder()
	                        .bookId(book.getId())
	                        .title(book.getTitle())
	                        .description(book.getDescription())
	                        .category(book.getCategory())
	                        .bookType(book.getBookType().name())
	                        .coverImageUrl(coverImageUrl)
	                        .startedAt(myReading.getCreatedAt())
	                        .completedAt(myReading.getCompletedAt())
	                        .readingTime(myReading.getReadingTime())
	                        .readingStatus(myReading.getReadingStatus().name())
	                        .rereadCount(myReading.getRereadCount())
	                        .build();

	            })
	            .collect(Collectors.toList());
	}

	@Override
	public void updateReadingProgress(Long memberId, Long bookId, ReadingProgressRequestDto requestDto) {

	    MyReading myReading = myReadingRepository
	            .findByMember_IdAndBook_Id(memberId, bookId)
	            .orElse(null);

	    if (myReading == null) {
	        Member member = memberRepository.findById(memberId)
	                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

	        Book book = bookRepository.findById(bookId)
	                .orElseThrow(() -> new CustomException(CommonErrorCode.BOOK_NOT_FOUND));

	        myReading = MyReading.builder()
	                .member(member)
	                .book(book)
	                .readingStatus(ReadingStatus.READING)
	                .currentPage(requestDto.getCurrentPage())
	                .progress(requestDto.getProgress())
	                .recentReadAt(LocalDateTime.now())
	                .rereadCount(0)
	                .readingTime(0)
	                .build();

	        myReadingRepository.save(myReading);
	        return;
	    }

	    if (myReading.getReadingStatus() == ReadingStatus.WISH) {
	        myReading.setReadingStatus(ReadingStatus.READING);
	    }

	    myReading.setCurrentPage(requestDto.getCurrentPage());
	    myReading.setProgress(requestDto.getProgress());
	    myReading.setRecentReadAt(LocalDateTime.now());
	}

	@Override
	public void completeReading(Long memberId, Long bookId){
		 MyReading myReading = myReadingRepository
		            .findByMember_IdAndBook_Id(memberId, bookId)
		            .orElseThrow(() -> new CustomException(ReadingErrorCode.MY_READING_NOT_FOUND));

		 LocalDateTime now = LocalDateTime.now();

		 
		 myReading.setReadingStatus(ReadingStatus.COMPLETED);
		 myReading.setCurrentPage(myReading.getBook().getPageCount());
		 myReading.setProgress(100);
		 myReading.setCompletedAt(now);

		 // 완독 횟수 증가
		 myReading.setRereadCount(myReading.getRereadCount() + 1);
		
	}

	@Override
	public void rereadBook(Long memberId, Long bookId){
		 MyReading myReading = myReadingRepository
		            .findByMember_IdAndBook_Id(memberId, bookId)
		            .orElseThrow(() -> new CustomException(ReadingErrorCode.MY_READING_NOT_FOUND));

		    myReading.setReadingStatus(ReadingStatus.READING);
		    myReading.setCurrentPage(1);
		    myReading.setProgress(0);
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
	            .bookType(book.getBookType().name())
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
	            .countByMember_IdAndRereadCountGreaterThan(memberId, 0);

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
