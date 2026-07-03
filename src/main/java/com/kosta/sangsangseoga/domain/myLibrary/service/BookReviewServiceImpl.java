package com.kosta.sangsangseoga.domain.myLibrary.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.book.repository.BookRepository;
import com.kosta.sangsangseoga.domain.myLibrary.dto.AiFeedbackResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.BookReviewRequestDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.BookReviewResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.entity.BookReview;
import com.kosta.sangsangseoga.domain.myLibrary.repository.BookReviewRepository;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
@Transactional
public class BookReviewServiceImpl implements BookReviewService {

	private final BookReviewRepository bookReviewRepository;
	private final BookRepository bookRepository;
	
	@Override
    @Transactional(readOnly = true)
    public List<BookReviewResponseDto> getReviews(Long memberId) throws Exception {
        return bookReviewRepository.findByMember_IdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BookReviewResponseDto getReview(Long memberId, Long reviewId) throws Exception {
        BookReview review = bookReviewRepository.findByIdAndMember_Id(reviewId, memberId)
                .orElseThrow(() -> new Exception("독후감을 찾을 수 없습니다."));

        return toResponseDto(review);
    }

    @Override
    public BookReviewResponseDto createReview(Long memberId, BookReviewRequestDto requestDto) throws Exception {
        bookReviewRepository.findByMember_IdAndBook_Id(memberId, requestDto.getBookId())
                .ifPresent(review -> {
                    throw new RuntimeException("이미 작성한 독후감이 있습니다.");
                });

        Book book = bookRepository.findById(requestDto.getBookId())
                .orElseThrow(() -> new Exception("책을 찾을 수 없습니다."));

        // Member 연동 후 여기서 member 조회 필요
        // Member member = memberRepository.findById(memberId).orElseThrow(...);

        BookReview review = BookReview.create(
                null,
                book,
                requestDto.getContent(),
                requestDto.getIsDraft()
        );

        BookReview saved = bookReviewRepository.save(review);

        return toResponseDto(saved);
    }

    @Override
    public BookReviewResponseDto updateReview(Long memberId, Long reviewId, BookReviewRequestDto requestDto) throws Exception {
        BookReview review = bookReviewRepository.findByIdAndMember_Id(reviewId, memberId)
                .orElseThrow(() -> new Exception("독후감을 찾을 수 없습니다."));

        review.updateReview(requestDto.getContent(), requestDto.getIsDraft());

        return toResponseDto(review);
    }

    @Override
    public void deleteReview(Long memberId, Long reviewId) throws Exception {
        BookReview review = bookReviewRepository.findByIdAndMember_Id(reviewId, memberId)
                .orElseThrow(() -> new Exception("독후감을 찾을 수 없습니다."));

        bookReviewRepository.delete(review);
    }

    @Override
    public BookReviewResponseDto saveDraft(Long memberId, Long reviewId, BookReviewRequestDto requestDto) throws Exception {
        BookReview review = bookReviewRepository.findByIdAndMember_Id(reviewId, memberId)
                .orElseThrow(() -> new Exception("독후감을 찾을 수 없습니다."));

        review.saveDraft(requestDto.getContent());

        return toResponseDto(review);
    }

    @Override
    @Transactional(readOnly = true)
    public AiFeedbackResponseDto getAiFeedback(Long memberId, Long reviewId) throws Exception {
        BookReview review = bookReviewRepository.findByIdAndMember_Id(reviewId, memberId)
                .orElseThrow(() -> new Exception("독후감을 찾을 수 없습니다."));

        return AiFeedbackResponseDto.builder()
                .reviewId(review.getId())
                .aiFeedbackContent(review.getAiFeedbackContent())
                .aiFeedbackCreatedAt(review.getAiFeedbackCreatedAt())
                .build();
    }

    private BookReviewResponseDto toResponseDto(BookReview review) {
        Book book = review.getBook();

        return BookReviewResponseDto.builder()
                .reviewId(review.getId())
                .bookId(book.getId())
                .bookTitle(book.getTitle())
                .coverImageId(book.getCoverImageId())
                .content(review.getContent())
                .isDraft(review.getIsDraft())
                .aiFeedbackContent(review.getAiFeedbackContent())
                .aiFeedbackCreatedAt(review.getAiFeedbackCreatedAt())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

}
