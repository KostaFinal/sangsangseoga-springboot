package com.kosta.sangsangseoga.domain.myLibrary.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kosta.sangsangseoga.domain.ai.service.GeminiService;
import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.book.entity.BookImage;
import com.kosta.sangsangseoga.domain.book.repository.BookImageRepository;
import com.kosta.sangsangseoga.domain.book.repository.BookRepository;
import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
import com.kosta.sangsangseoga.domain.myLibrary.dto.AiFeedbackResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.BookReviewRequestDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.BookReviewResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.entity.BookReview;
import com.kosta.sangsangseoga.domain.myLibrary.exception.ReadingErrorCode;
import com.kosta.sangsangseoga.domain.myLibrary.repository.BookReviewRepository;
import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BookReviewServiceImpl implements BookReviewService {

	private final BookReviewRepository bookReviewRepository;
	private final BookRepository bookRepository;
	private final MemberRepository memberRepository;
	private final GeminiService geminiService;
	private final BookImageRepository bookImageRepository;

	@Override
	@Transactional(readOnly = true)
	public List<BookReviewResponseDto> getReviews(Long memberId) {
	    List<BookReview> reviews =
	            bookReviewRepository.findByMember_IdOrderByCreatedAtDesc(memberId);

	    if (reviews.isEmpty()) {
	        return Collections.emptyList();
	    }

	    List<Book> books = reviews.stream()
	            .map(BookReview::getBook)
	            .distinct()
	            .collect(Collectors.toList());

	    Map<Long, String> coverImageUrlMap = bookImageRepository
	            .findByBookInAndImageTypeAndDeletedAtIsNull(
	                    books,
	                    BookImage.ImageType.COVER
	            )
	            .stream()
	            .collect(Collectors.toMap(
	                    image -> image.getBook().getId(),
	                    BookImage::getFileUrl,
	                    (existing, replacement) -> existing
	            ));

	    return reviews.stream()
	            .map(review -> toResponseDto(review, coverImageUrlMap))
	            .collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public BookReviewResponseDto getReview(Long memberId, Long reviewId) {
		BookReview review = bookReviewRepository.findByIdAndMember_Id(reviewId, memberId)
				.orElseThrow(() -> new CustomException(ReadingErrorCode.BOOK_REVIEW_NOT_FOUND));

		return toResponseDto(review);
	}

	@Override
	public BookReviewResponseDto createReview(Long memberId, BookReviewRequestDto requestDto) {
		bookReviewRepository.findByMember_IdAndBook_Id(memberId, requestDto.getBookId()).ifPresent(review -> {
			throw new CustomException(ReadingErrorCode.BOOK_REVIEW_ALREADY_EXISTS);
		});

		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

		Book book = bookRepository.findById(requestDto.getBookId())
				.orElseThrow(() -> new CustomException(CommonErrorCode.BOOK_NOT_FOUND));

		BookReview review = BookReview.builder().member(member).book(book).content(requestDto.getContent())
				.isDraft(requestDto.getIsDraft() != null ? requestDto.getIsDraft() : false).build();

		BookReview saved = bookReviewRepository.save(review);

		return toResponseDto(saved);
	}

	@Override
	public BookReviewResponseDto updateReview(Long memberId, Long reviewId, BookReviewRequestDto requestDto) {
		BookReview review = bookReviewRepository.findByIdAndMember_Id(reviewId, memberId)
				.orElseThrow(() -> new CustomException(ReadingErrorCode.BOOK_REVIEW_NOT_FOUND));

		review.setContent(requestDto.getContent());
		review.setIsDraft(requestDto.getIsDraft() != null ? requestDto.getIsDraft() : false);

		return toResponseDto(review);
	}

	@Override
	public void deleteReview(Long memberId, Long reviewId) {
		BookReview review = bookReviewRepository.findByIdAndMember_Id(reviewId, memberId)
				.orElseThrow(() -> new CustomException(ReadingErrorCode.BOOK_REVIEW_NOT_FOUND));

		bookReviewRepository.delete(review);
	}

	@Override
	public BookReviewResponseDto saveDraft(Long memberId, Long reviewId, BookReviewRequestDto requestDto) {
		BookReview review = bookReviewRepository.findByIdAndMember_Id(reviewId, memberId)
				.orElseThrow(() -> new CustomException(ReadingErrorCode.BOOK_REVIEW_NOT_FOUND));

		review.setContent(requestDto.getContent());
		review.setIsDraft(true);

		return toResponseDto(review);
	}

	@Override
	@Transactional(readOnly = true)
	public AiFeedbackResponseDto getAiFeedback(Long memberId, Long reviewId) {
		BookReview review = bookReviewRepository.findByIdAndMember_Id(reviewId, memberId)
				.orElseThrow(() -> new CustomException(ReadingErrorCode.BOOK_REVIEW_NOT_FOUND));

		return AiFeedbackResponseDto.builder().reviewId(review.getId()).aiFeedbackContent(review.getAiFeedbackContent())
				.aiFeedbackCreatedAt(review.getAiFeedbackCreatedAt()).build();
	}

	private BookReviewResponseDto toResponseDto(BookReview review) {
	    Book book = review.getBook();

	    String coverImageUrl = bookImageRepository
	            .findByBookAndImageTypeAndDeletedAtIsNull(
	                    book,
	                    BookImage.ImageType.COVER
	            )
	            .map(BookImage::getFileUrl)
	            .orElse(null);

	    return BookReviewResponseDto.builder()
	            .reviewId(review.getId())
	            .bookId(book.getId())
	            .bookTitle(book.getTitle())
	            .coverImageUrl(coverImageUrl)
	            .content(review.getContent())
	            .isDraft(review.getIsDraft())
	            .aiFeedbackContent(review.getAiFeedbackContent())
	            .aiFeedbackCreatedAt(review.getAiFeedbackCreatedAt())
	            .createdAt(review.getCreatedAt())
	            .updatedAt(review.getUpdatedAt())
	            .build();
	}
	
	private BookReviewResponseDto toResponseDto(
	        BookReview review,
	        Map<Long, String> coverImageUrlMap
	) {
	    Book book = review.getBook();

	    return BookReviewResponseDto.builder()
	            .reviewId(review.getId())
	            .bookId(book.getId())
	            .bookTitle(book.getTitle())
	            .coverImageUrl(coverImageUrlMap.get(book.getId()))
	            .content(review.getContent())
	            .isDraft(review.getIsDraft())
	            .aiFeedbackContent(review.getAiFeedbackContent())
	            .aiFeedbackCreatedAt(review.getAiFeedbackCreatedAt())
	            .createdAt(review.getCreatedAt())
	            .updatedAt(review.getUpdatedAt())
	            .build();
	}

	@Override
	public AiFeedbackResponseDto requestAiFeedback(Long memberId, Long reviewId) {
		BookReview review = bookReviewRepository.findByIdAndMember_Id(reviewId, memberId)
				.orElseThrow(() -> new CustomException(ReadingErrorCode.BOOK_REVIEW_NOT_FOUND));

		String feedback = geminiService.generateReviewFeedback(review.getBook().getTitle(), review.getContent());

		review.updateAiFeedback(feedback);

		return AiFeedbackResponseDto.builder().reviewId(review.getId()).aiFeedbackContent(review.getAiFeedbackContent())
				.aiFeedbackCreatedAt(review.getAiFeedbackCreatedAt()).build();
	}

}
