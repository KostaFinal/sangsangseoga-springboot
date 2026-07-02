package com.kosta.sangsangseoga.domain.myLibrary.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
		return null;
	}

	@Override
	public void deleteWishlist(Long memberId, Long bookId) throws Exception {

	}

	@Override
	public List<ReadingBookResponseDto> getReadingList(Long memberId) throws Exception {
		return null;
	}

	@Override
	public List<FinishedBookResponseDto> getFinishedList(Long memberId) throws Exception {
		return null;
	}

	@Override
	public void updateReadingProgress(Long memberId, Long bookId, ReadingProgressRequestDto readingProgressRequestDto)
			throws Exception {
		MyReading myReading = myReadingRepository.findByMember_IdAndBook_IdAndReadingStatus(memberId, bookId, ReadingStatus.READING).orElseThrow(()-> new Exception("읽는 중인 책이 존재하지 않습니다."));
		myReading.updateProgress(readingProgressRequestDto.getCurrentPage(), readingProgressRequestDto.getProgress());
	}

}
