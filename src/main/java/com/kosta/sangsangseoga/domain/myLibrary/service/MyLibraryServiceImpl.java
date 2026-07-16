package com.kosta.sangsangseoga.domain.myLibrary.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.book.entity.BookImage;
import com.kosta.sangsangseoga.domain.book.entity.BookTag;
import com.kosta.sangsangseoga.domain.book.enums.BookStatus;
import com.kosta.sangsangseoga.domain.book.repository.BookImageRepository;
import com.kosta.sangsangseoga.domain.book.repository.BookRepository;
import com.kosta.sangsangseoga.domain.book.repository.BookTagRepository;
import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
import com.kosta.sangsangseoga.domain.myLibrary.dto.CategoryStatsDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.FinishedBookResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.MyWrittenBookResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.ReadingBookResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.ReadingProgressRequestDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.ReadingStatsResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.UpdateBookDescriptionRequestDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.UpdateBookStatusRequestDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.WishlistBookResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.entity.MyReading;
import com.kosta.sangsangseoga.domain.myLibrary.enums.ReadingStatus;
import com.kosta.sangsangseoga.domain.myLibrary.exception.ReadingErrorCode;
import com.kosta.sangsangseoga.domain.myLibrary.repository.BookReviewRepository;
import com.kosta.sangsangseoga.domain.myLibrary.repository.MyReadingRepository;
import com.kosta.sangsangseoga.domain.subscription.dto.UpdateBookTagsRequestDto;
import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MyLibraryServiceImpl implements MyLibraryService {

	private final MyReadingRepository myReadingRepository;
	private final BookReviewRepository bookReviewRepository;
	private final BookImageRepository bookImageRepository;
	private final BookRepository bookRepository;
	private final MemberRepository memberRepository;
	private final BookTagRepository bookTagRepository;

	private Map<Long, String> getCoverImageUrlMap(List<Book> books) {
		if (books.isEmpty()) {
			return Map.of();
		}

		return bookImageRepository.findByBookInAndImageTypeAndDeletedAtIsNull(books, BookImage.ImageType.COVER).stream()
				.collect(Collectors.toMap(bookImage -> bookImage.getBook().getId(), BookImage::getFileUrl,
						(existing, replacement) -> existing));
	}

	@Override
	@Transactional(readOnly = true)
	public List<WishlistBookResponseDto> getWishlist(Long memberId) {
		List<MyReading> myReadings = myReadingRepository.findByMember_IdAndWishlistTrue(memberId);

		List<Book> books = myReadings.stream().map(MyReading::getBook).collect(Collectors.toList());

		Map<Long, String> coverImageUrlMap = getCoverImageUrlMap(books);

		return myReadings.stream().map(myReading -> {
			Book book = myReading.getBook();

			return WishlistBookResponseDto.builder().bookId(book.getId()).title(book.getTitle())
					.description(book.getDescription()).category(book.getCategory()).bookType(book.getBookType().name())
					.coverImageUrl(coverImageUrlMap.get(book.getId())).wishlist(myReading.getWishlist()).build();
		}).collect(Collectors.toList());
	}

	@Override
	public void addWishlist(Long memberId, Long bookId) {
		MyReading existing = myReadingRepository.findByMember_IdAndBook_Id(memberId, bookId).orElse(null);

		if (existing != null) {
			if (Boolean.TRUE.equals(existing.getWishlist())) {
				return;
			}

			existing.addToWishlist();
			return;

		}

		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

		Book book = bookRepository.findById(bookId)
				.orElseThrow(() -> new CustomException(CommonErrorCode.BOOK_NOT_FOUND));

		MyReading myReading = MyReading.builder().member(member).book(book).wishlist(true).currentPage(1).progress(0)
				.rereadCount(0).readingTime(0).build();

		myReadingRepository.save(myReading);
	}

	@Override
	public void deleteWishlist(Long memberId, Long bookId) {
		MyReading myReading = myReadingRepository.findByMember_IdAndBook_IdAndWishlistTrue(memberId, bookId)
				.orElseThrow(() -> new CustomException(ReadingErrorCode.WISHLIST_NOT_FOUND));

		myReading.removeFromWishlist();
	}

	@Override
	@Transactional(readOnly = true)
	public List<ReadingBookResponseDto> getReadingList(Long memberId) {
		List<MyReading> myReadings = myReadingRepository
		        .findByMember_IdAndReadingStatusAndBook_StatusNotOrderByRecentReadAtDesc(
		                memberId,
		                ReadingStatus.READING,
		                BookStatus.DELETED
		        );

		List<Book> books = myReadings.stream().map(MyReading::getBook).collect(Collectors.toList());

		Map<Long, String> coverImageUrlMap = getCoverImageUrlMap(books);

		return myReadings.stream().map(myReading -> {
			Book book = myReading.getBook();

			return ReadingBookResponseDto.builder().bookId(book.getId()).title(book.getTitle())
					.description(book.getDescription()).category(book.getCategory()).bookType(book.getBookType().name())
					.coverImageUrl(coverImageUrlMap.get(book.getId())).currentPage(myReading.getCurrentPage())
					.progress(myReading.getProgress()).pageCount(book.getPageCount()).readingStatus(myReading.getReadingStatus().name())
			        .recentReadAt(myReading.getRecentReadAt()).build();
		}).collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public List<FinishedBookResponseDto> getFinishedList(Long memberId) {
		List<MyReading> myReadings = myReadingRepository
				.findByMember_IdAndRereadCountGreaterThanOrderByCompletedAtDesc(memberId, 0);

		List<Book> books = myReadings.stream().map(MyReading::getBook).collect(Collectors.toList());

		Map<Long, String> coverImageUrlMap = getCoverImageUrlMap(books);

		return myReadings.stream().map(myReading -> {
			Book book = myReading.getBook();

			return FinishedBookResponseDto.builder().bookId(book.getId()).title(book.getTitle())
					.description(book.getDescription()).category(book.getCategory()).bookType(book.getBookType().name())
					.coverImageUrl(coverImageUrlMap.get(book.getId())).startedAt(myReading.getCreatedAt())
					.completedAt(myReading.getCompletedAt()).readingTime(myReading.getReadingTime())
					.readingStatus(myReading.getReadingStatus().name()).rereadCount(myReading.getRereadCount())
					.viewCount(book.getViewCount()).likeCount(book.getLikeCount()).build();
		}).collect(Collectors.toList());
	}

	@Override
	public void updateReadingProgress(Long memberId, Long bookId, ReadingProgressRequestDto requestDto) {

		MyReading myReading = myReadingRepository.findByMember_IdAndBook_Id(memberId, bookId).orElse(null);

		if (myReading == null) {
			Member member = memberRepository.findById(memberId)
					.orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

			Book book = bookRepository.findById(bookId)
					.orElseThrow(() -> new CustomException(CommonErrorCode.BOOK_NOT_FOUND));

			myReading = MyReading.builder().member(member).book(book).readingStatus(ReadingStatus.READING)
					.currentPage(requestDto.getCurrentPage()).progress(requestDto.getProgress())
					.recentReadAt(LocalDateTime.now()).rereadCount(0).readingTime(0).build();

			myReadingRepository.save(myReading);
			return;
		}

		if (myReading.getReadingStatus() == null) {
			myReading.setReadingStatus(ReadingStatus.READING);
		}

		myReading.setCurrentPage(requestDto.getCurrentPage());
		myReading.setProgress(requestDto.getProgress());
		myReading.setRecentReadAt(LocalDateTime.now());

		Integer addedReadingTime = requestDto.getReadingTime();

		if (addedReadingTime != null && addedReadingTime > 0) {
			Integer currentReadingTime = myReading.getReadingTime() != null ? myReading.getReadingTime() : 0;

			myReading.setReadingTime(currentReadingTime + addedReadingTime);
		}
	}

	@Override
	public void completeReading(Long memberId, Long bookId) {
		MyReading myReading = myReadingRepository.findByMember_IdAndBook_Id(memberId, bookId)
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
	public void rereadBook(Long memberId, Long bookId) {
		MyReading myReading = myReadingRepository.findByMember_IdAndBook_Id(memberId, bookId)
				.orElseThrow(() -> new CustomException(ReadingErrorCode.MY_READING_NOT_FOUND));

		myReading.setReadingStatus(ReadingStatus.READING);
		myReading.setCurrentPage(1);
		myReading.setProgress(0);
		myReading.setRecentReadAt(LocalDateTime.now());

	}

	@Override
	@Transactional(readOnly = true)
	public ReadingBookResponseDto getLastReadingPosition(Long memberId, Long bookId) {
		MyReading myReading = myReadingRepository.findByMember_IdAndBook_Id(memberId, bookId)
				.orElseThrow(() -> new CustomException(ReadingErrorCode.MY_READING_NOT_FOUND));

		Book book = myReading.getBook();

		return ReadingBookResponseDto.builder().bookId(book.getId()).title(book.getTitle()).category(book.getCategory())
				.bookType(book.getBookType().name()).currentPage(myReading.getCurrentPage())
				.progress(myReading.getProgress()).pageCount(book.getPageCount())
				.readingStatus(myReading.getReadingStatus().name()).recentReadAt(myReading.getRecentReadAt()).build();
	}

	@Override
	@Transactional(readOnly = true)
	public ReadingStatsResponseDto getReadingStats(Long memberId) {
		Long wishlistBookCount = myReadingRepository.countByMember_IdAndWishlistTrue(memberId);

		Long readingBookCount = myReadingRepository.countByMember_IdAndReadingStatus(memberId, ReadingStatus.READING);

		Long completedBookCount = myReadingRepository.countByMember_IdAndRereadCountGreaterThan(memberId, 0);

		Long totalReadingTime = myReadingRepository.sumReadingTimeByMemberId(memberId);

		Long reportCount = bookReviewRepository.countByMember_Id(memberId);

		List<MyReading> myReadings = myReadingRepository.findByMember_IdAndRereadCountGreaterThan(memberId, 0);

		Long totalPagesRead = myReadings.stream().mapToLong(myReading -> {
			Book book = myReading.getBook();

			int pageCount = book.getPageCount() != null ? book.getPageCount() : 0;
			int progress = myReading.getProgress() != null ? myReading.getProgress() : 0;

			return Math.round(pageCount * (progress / 100.0));
		}).sum();

		List<CategoryStatsDto> categoryStats = myReadings.stream().collect(Collectors.groupingBy(
				myReading -> myReading.getBook().getCategory() != null ? myReading.getBook().getCategory() : "기타",
				Collectors.counting())).entrySet().stream()
				.map(entry -> CategoryStatsDto.builder().category(entry.getKey()).count(entry.getValue()).build())
				.collect(Collectors.toList());

		List<Book> writtenBooks = bookRepository.findByMember_IdAndStatus(memberId, BookStatus.PUBLISHED);

		Long writtenBookCount = (long) writtenBooks.size();

		List<CategoryStatsDto> writtenCategoryStats = writtenBooks.stream()
				.collect(Collectors.groupingBy(book -> book.getCategory() != null ? book.getCategory() : "기타",
						Collectors.counting()))
				.entrySet().stream()
				.map(entry -> CategoryStatsDto.builder().category(entry.getKey()).count(entry.getValue()).build())
				.collect(Collectors.toList());

		List<FinishedBookResponseDto> finishedBooks = getFinishedList(memberId);

		return ReadingStatsResponseDto.builder().totalReadingTime(totalReadingTime).totalPagesRead(totalPagesRead)
				.reportCount(reportCount).wishlistBookCount(wishlistBookCount).readingBookCount(readingBookCount)
				.completedBookCount(completedBookCount).categoryStats(categoryStats).finishedBooks(finishedBooks)
				.writtenBookCount(writtenBookCount).writtenCategoryStats(writtenCategoryStats).build();
	}

	@Override
	@Transactional(readOnly = true)
	public Page<MyWrittenBookResponseDto> getMyWrittenBooks(
	        Long memberId,
	        int page,
	        int size
	) {
		Page<Book> bookPage = bookRepository
		        .findByMember_IdAndStatusNotOrderByCreatedAtDesc(
		                memberId,
		                BookStatus.DELETED,
		                PageRequest.of(page - 1, size)
		        );

	    List<Book> books = bookPage.getContent();
	    Map<Long, String> coverImageUrlMap = getCoverImageUrlMap(books);

	    return bookPage.map(book ->
	            MyWrittenBookResponseDto.builder()
	                    .bookId(book.getId())
	                    .title(book.getTitle())
	                    .description(book.getDescription())
	                    .category(book.getCategory())
	                    .bookType(
	                            book.getBookType() != null
	                                    ? book.getBookType().name()
	                                    : null
	                    )
	                    .coverImageUrl(coverImageUrlMap.get(book.getId()))
	                    .pageCount(book.getPageCount())
	                    .viewCount(book.getViewCount())
	                    .likeCount(book.getLikeCount())
	                    .status(
	                            book.getStatus() != null
	                                    ? book.getStatus().name()
	                                    : null
	                    )
	                    .build()
	    );
	}

	@Override

	public void updateMyWrittenBookStatus(
	        Long memberId,
	        Long bookId,
	        UpdateBookStatusRequestDto requestDto
	) {
	    Book book = bookRepository.findById(bookId)
	            .orElseThrow(() ->
	                    new CustomException(CommonErrorCode.BOOK_NOT_FOUND)
	            );


		if (!book.getMember().getId().equals(memberId)) {
			throw new CustomException(CommonErrorCode.FORBIDDEN);
		}


	    BookStatus status;

	    try {
	        status = BookStatus.valueOf(requestDto.getStatus());
	    } catch (IllegalArgumentException | NullPointerException e) {
	        log.warn(
	                "Invalid book status value: {}",
	                requestDto.getStatus()
	        );
	        throw new CustomException(CommonErrorCode.BAD_REQUEST);
	    }

	    book.setStatus(status);

	    

	}

	@Override
	public void updateMyWrittenBookDescription(Long memberId, Long bookId, UpdateBookDescriptionRequestDto requestDto) {
		Book book = bookRepository.findById(bookId)
				.orElseThrow(() -> new CustomException(CommonErrorCode.BOOK_NOT_FOUND));

		if (!book.getMember().getId().equals(memberId)) {
			throw new CustomException(CommonErrorCode.FORBIDDEN);
		}

		book.setDescription(requestDto.getDescription());
	}
	
	@Override
	public void updateMyWrittenBookTags(
	        Long memberId,
	        Long bookId,
	        UpdateBookTagsRequestDto requestDto
	) {
	    Book book = bookRepository.findById(bookId)
	            .orElseThrow(() ->
	                    new CustomException(CommonErrorCode.BOOK_NOT_FOUND)
	            );

	    if (!book.getMember().getId().equals(memberId)) {
	        throw new CustomException(CommonErrorCode.FORBIDDEN);
	    }

	    List<String> normalizedTags = requestDto.getTags().stream()
	            .filter(tag -> tag != null && !tag.isBlank())
	            .map(String::trim)
	            .map(tag -> tag.startsWith("#")
	                    ? tag.substring(1).trim()
	                    : tag)
	            .filter(tag -> !tag.isBlank())
	            .distinct()
	            .collect(Collectors.toList());

	    if (normalizedTags.size() > 10) {
	        throw new CustomException(CommonErrorCode.BAD_REQUEST);
	    }

	    boolean invalidTagExists = normalizedTags.stream()
	            .anyMatch(tag -> tag.length() > 50);

	    if (invalidTagExists) {
	        throw new CustomException(CommonErrorCode.BAD_REQUEST);
	    }

	    bookTagRepository.deleteByBook(book);
	    
	 // 기존 태그 DELETE SQL을 새 태그 INSERT보다 먼저 실행
	    bookTagRepository.flush();

	    List<BookTag> newBookTags = normalizedTags.stream()
	            .map(tagName ->
	                    BookTag.builder()
	                            .book(book)
	                            .tagName(tagName)
	                            .build()
	            )
	            .collect(Collectors.toList());

	    bookTagRepository.saveAll(newBookTags);
	}
	
	@Override
	public void deleteMyWrittenBook(Long memberId, Long bookId) {
	    Book book = bookRepository.findById(bookId)
	            .orElseThrow(() ->
	                    new CustomException(CommonErrorCode.BOOK_NOT_FOUND)
	            );

	    if (!book.getMember().getId().equals(memberId)) {
	        throw new CustomException(CommonErrorCode.FORBIDDEN);
	    }

	    if (book.getStatus() == BookStatus.DELETED) {
	        throw new CustomException(CommonErrorCode.BOOK_NOT_FOUND);
	    }

	    book.setStatus(BookStatus.DELETED);
	}

}
