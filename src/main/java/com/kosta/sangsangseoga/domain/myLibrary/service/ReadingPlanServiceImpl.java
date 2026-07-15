package com.kosta.sangsangseoga.domain.myLibrary.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.book.entity.BookImage;
import com.kosta.sangsangseoga.domain.book.repository.BookImageRepository;
import com.kosta.sangsangseoga.domain.book.repository.BookRepository;
import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
import com.kosta.sangsangseoga.domain.myLibrary.dto.ReadingPlanRequestDto;
import com.kosta.sangsangseoga.domain.myLibrary.dto.ReadingPlanResponseDto;
import com.kosta.sangsangseoga.domain.myLibrary.entity.ReadingPlan;
import com.kosta.sangsangseoga.domain.myLibrary.exception.ReadingErrorCode;
import com.kosta.sangsangseoga.domain.myLibrary.repository.ReadingPlanRepository;
import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
@Transactional
public class ReadingPlanServiceImpl implements ReadingPlanService {

	// 독서 계획 Repository
    private final ReadingPlanRepository readingPlanRepository;

    // 회원 조회 Repository
    private final MemberRepository memberRepository;

    // 도서 조회 Repository
    private final BookRepository bookRepository;
    
    private final BookImageRepository bookImageRepository;
    
    private Map<Long, String> buildCoverImageMap(List<ReadingPlan> readingPlans) {
    	
    	if (readingPlans == null || readingPlans.isEmpty()) {
            return Map.of();
        }
    	
        List<Book> books = readingPlans.stream()
                .map(ReadingPlan::getBook)
                .collect(Collectors.toList());

        return bookImageRepository
                .findByBookInAndImageTypeAndDeletedAtIsNull(books, BookImage.ImageType.COVER)
                .stream()
                .collect(Collectors.toMap(
                        image -> image.getBook().getId(),
                        BookImage::getFileUrl,
                        (existing, replacement) -> existing
                ));
    }
    
    /**
     * Entity → ResponseDto 변환
     */
    private ReadingPlanResponseDto toResponseDto(ReadingPlan readingPlan) {

        Map<Long, String> coverImageMap = buildCoverImageMap(List.of(readingPlan));
        return toResponseDto(readingPlan, coverImageMap);


        

    }
    
    private ReadingPlanResponseDto toResponseDto(
            ReadingPlan readingPlan,
            Map<Long, String> coverImageMap) {

        Book book = readingPlan.getBook();

        return ReadingPlanResponseDto.builder()
                .planId(readingPlan.getId())
                .bookId(book.getId())
                .bookTitle(book.getTitle())
                .category(book.getCategory())
                .coverImageUrl(coverImageMap.get(book.getId()))
                .planDate(readingPlan.getPlanDate())
                .targetPage(readingPlan.getTargetPage())
                .memo(readingPlan.getMemo())
                .isCompleted(readingPlan.getIsCompleted())
                .completedAt(readingPlan.getCompletedAt())
                .build();
    }

    /**
     * 전체 독서 계획 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<ReadingPlanResponseDto> getReadingPlans(Long memberId){

    	List<ReadingPlan> readingPlans =
    	        readingPlanRepository.findByMember_IdOrderByPlanDateAsc(memberId);

    	Map<Long, String> coverImageMap = buildCoverImageMap(readingPlans);

    	return readingPlans.stream()
    	        .map(plan -> toResponseDto(plan, coverImageMap))
    	        .collect(Collectors.toList());
    }

    /**
     * 특정 날짜의 독서 계획 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<ReadingPlanResponseDto> getReadingPlansByDate(Long memberId, LocalDate planDate){

    	List<ReadingPlan> readingPlans =
    	        readingPlanRepository.findByMember_IdAndPlanDateOrderByIdAsc(memberId, planDate);

    	Map<Long, String> coverImageMap = buildCoverImageMap(readingPlans);

    	return readingPlans.stream()
    	        .map(plan -> toResponseDto(plan, coverImageMap))
    	        .collect(Collectors.toList());
    }

    /**
     * 독서 계획 등록
     */
    @Override
    public ReadingPlanResponseDto createReadingPlan(Long memberId, ReadingPlanRequestDto requestDto){

        // 같은 날짜에 같은 책이 이미 등록되어 있는지 확인
        readingPlanRepository.findByMember_IdAndBook_IdAndPlanDate(
                memberId,
                requestDto.getBookId(),
                requestDto.getPlanDate())
        .ifPresent(plan -> {
        	throw new CustomException(ReadingErrorCode.READING_PLAN_ALREADY_EXISTS);
        });

        // 회원 조회
        Member member = memberRepository.findById(memberId)
        		.orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        // 도서 조회
        Book book = bookRepository.findById(requestDto.getBookId())
        		.orElseThrow(() -> new CustomException(CommonErrorCode.BOOK_NOT_FOUND));

        // 독서 계획 생성
        ReadingPlan readingPlan = ReadingPlan.builder()
                .member(member)
                .book(book)
                .planDate(requestDto.getPlanDate())
                .targetPage(requestDto.getTargetPage())
                .memo(requestDto.getMemo())
                .isCompleted(false)
                .build();

        return toResponseDto(readingPlanRepository.save(readingPlan));
    }

    /**
     * 독서 계획 수정
     */
    @Override
    public ReadingPlanResponseDto updateReadingPlan(
            Long memberId,
            Long planId,
            ReadingPlanRequestDto requestDto){

        // 수정할 독서 계획 조회
        ReadingPlan readingPlan = readingPlanRepository.findByIdAndMember_Id(planId, memberId)
        		.orElseThrow(() -> new CustomException(ReadingErrorCode.READING_PLAN_NOT_FOUND));

        // 계획 정보 수정
        readingPlan.setPlanDate(requestDto.getPlanDate());
        readingPlan.setTargetPage(requestDto.getTargetPage());
        readingPlan.setMemo(requestDto.getMemo());

        return toResponseDto(readingPlan);
    }

    /**
     * 독서 계획 삭제
     */
    @Override
    public void deleteReadingPlan(Long memberId, Long planId){

        // 삭제할 독서 계획 조회
        ReadingPlan readingPlan = readingPlanRepository.findByIdAndMember_Id(planId, memberId)
        		.orElseThrow(() -> new CustomException(ReadingErrorCode.READING_PLAN_NOT_FOUND));

        readingPlanRepository.delete(readingPlan);
    }

    /**
     * 독서 계획 완료 처리
     */
    @Override
    public ReadingPlanResponseDto completeReadingPlan(Long memberId, Long planId) {

        // 완료 처리할 독서 계획 조회
        ReadingPlan readingPlan = readingPlanRepository.findByIdAndMember_Id(planId, memberId)
        		.orElseThrow(() -> new CustomException(ReadingErrorCode.READING_PLAN_NOT_FOUND));

        // 완료 상태 변경
        readingPlan.complete();

        return toResponseDto(readingPlan);
    }

    

}
