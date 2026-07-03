package com.kosta.sangsangseoga.domain.myLibrary.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.myLibrary.dto.FinishedBookResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.ReadingBookResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.ReadingProgressRequestDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.WishlistBookResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.entity.MyReading;
import com.kosta.sangsangseoga.domain.myLibrary.enums.ReadingStatus;
import com.kosta.sangsangseoga.domain.myLibrary.repository.MyReadingRepository;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
@Transactional
public class MyLibraryServiceImpl implements MyLibraryService {
	
	private final MyReadingRepository myReadingRepository;

	@Override
	@Transactional(readOnly = true)
	public List<WishlistBookResponseDto> getWishlist(Long memberId) throws Exception {
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
							.genre(book.getGenre())
							.build();
				})
				.collect(Collectors.toList());
	}

	@Override
	public void deleteWishlist(Long memberId, Long bookId) throws Exception {
		MyReading myReading = myReadingRepository
	            .findByMember_IdAndBook_IdAndReadingStatus(
	                    memberId,
	                    bookId,
	                    ReadingStatus.WISH
	            )
	            .orElseThrow(() -> new Exception("읽고 싶은 책이 존재하지 않습니다."));

	    myReadingRepository.delete(myReading);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ReadingBookResponseDto> getReadingList(Long memberId) throws Exception {
		return myReadingRepository
				.findByMember_IdAndReadingStatus(memberId, ReadingStatus.READING)
				.stream()
				.map(myReading -> {
					Book book = myReading.getBook();
					
					return ReadingBookResponseDto.builder()
							.bookId(book.getId())
							.title(book.getTitle())
							.category(book.getCategory())
							.genre(book.getGenre())
							.currentPage(myReading.getCurrentPage())
							.progress(myReading.getProgress())
							.pageCount(book.getPageCount())
							.build();
				})
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public List<FinishedBookResponseDto> getFinishedList(Long memberId) throws Exception {
		 return myReadingRepository
		            .findByMember_IdAndReadingStatus(memberId, ReadingStatus.COMPLETED)
		            .stream()
		            .map(myReading -> {
		                Book book = myReading.getBook();

		                return FinishedBookResponseDto.builder()
		                        .bookId(book.getId())
		                        .title(book.getTitle())
		                        .category(book.getCategory())
		                        .genre(book.getGenre())
		                        .completedAt(myReading.getCompletedAt())
		                        .readingTime(myReading.getReadingTime())
		                        .build();
		            })
		            .collect(Collectors.toList());
	}

	@Override
	public void updateReadingProgress(Long memberId, Long bookId, ReadingProgressRequestDto readingProgressRequestDto)
			throws Exception {
		MyReading myReading = myReadingRepository.findByMember_IdAndBook_IdAndReadingStatus(memberId, bookId, ReadingStatus.READING).orElseThrow(()-> new Exception("읽는 중인 책이 존재하지 않습니다."));
		myReading.updateProgress(readingProgressRequestDto.getCurrentPage(), readingProgressRequestDto.getProgress());
	}

	@Override
	public void completeReading(Long memberId, Long bookId) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void rereadBook(Long memberId, Long bookId) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
