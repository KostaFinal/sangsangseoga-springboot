package com.kosta.sangsangseoga.domain.myLibrary.service;

import com.kosta.sangsangseoga.domain.account.entity.Member;
import com.kosta.sangsangseoga.domain.account.repository.MemberRepository;
import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.book.repository.BookRepository;
import com.kosta.sangsangseoga.domain.myLibrary.dto.ReadingMemoDto;
import com.kosta.sangsangseoga.domain.myLibrary.entity.ReadingMemo;
import com.kosta.sangsangseoga.domain.myLibrary.exception.ReadingErrorCode;
import com.kosta.sangsangseoga.domain.myLibrary.repository.ReadingMemoRepository;
import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class ReadingMemoServiceImpl implements ReadingMemoService {

    private final ReadingMemoRepository readingMemoRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;

    /**
     * 메모 조회
     * - 메모 없으면 null 반환 (프론트에서 data: null로 처리)
     */
    @Override
    @Transactional(readOnly = true)
    public ReadingMemoDto getMemo(Long memberId, Long bookId, Integer pageNo) throws Exception {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.BOOK_NOT_FOUND));

        return readingMemoRepository.findByMemberAndBookAndPageNo(member, book, pageNo)
                .map(memo -> ReadingMemoDto.builder()
                        .id(memo.getId())
                        .bookId(bookId)
                        .pageNo(pageNo)
                        .content(memo.getContent())
                        .posX(memo.getPosX())
                        .posY(memo.getPosY())
                        .createdAt(memo.getCreatedAt())
                        .updatedAt(memo.getUpdatedAt())
                        .build())
                .orElse(null);
    }

    /**
     * 메모 작성
     * - 이미 해당 페이지에 메모가 있으면 예외 발생
     */
    @Override
    public ReadingMemoDto addMemo(Long memberId, Long bookId, Integer pageNo, String content, BigDecimal posX, BigDecimal posY) throws Exception {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.BOOK_NOT_FOUND));

        // 중복 메모 방지
        if (readingMemoRepository.existsByMemberAndBookAndPageNo(member, book, pageNo)) {
            throw new CustomException(ReadingErrorCode.MEMO_ALREADY_EXISTS);
        }

        ReadingMemo memo = readingMemoRepository.save(ReadingMemo.builder()
                .member(member)
                .book(book)
                .pageNo(pageNo)
                .content(content)
                .posX(posX)
                .posY(posY)
                .build());

        return ReadingMemoDto.builder()
                .id(memo.getId())
                .bookId(bookId)
                .pageNo(pageNo)
                .content(memo.getContent())
                .posX(memo.getPosX())
                .posY(memo.getPosY())
                .createdAt(memo.getCreatedAt())
                .updatedAt(memo.getUpdatedAt())
                .build();
    }

    /**
     * 메모 수정
     * - 메모가 없으면 예외 발생
     */
    @Override
    public ReadingMemoDto updateMemo(Long memberId, Long bookId, Integer pageNo, String content, BigDecimal posX, BigDecimal posY) throws Exception {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.BOOK_NOT_FOUND));

        ReadingMemo memo = readingMemoRepository.findByMemberAndBookAndPageNo(member, book, pageNo)
                .orElseThrow(() -> new CustomException(ReadingErrorCode.MEMO_NOT_FOUND));

        // null이 아닌 값만 수정
        if (content != null) memo.setContent(content);
        if (posX != null) memo.setPosX(posX);
        if (posY != null) memo.setPosY(posY);

        return ReadingMemoDto.builder()
                .id(memo.getId())
                .bookId(bookId)
                .pageNo(pageNo)
                .content(memo.getContent())
                .posX(memo.getPosX())
                .posY(memo.getPosY())
                .createdAt(memo.getCreatedAt())
                .updatedAt(memo.getUpdatedAt())
                .build();
    }

    /**
     * 메모 삭제
     * - 메모가 없으면 예외 발생
     * - 204 응답이라 반환값 없음
     */
    @Override
    public void deleteMemo(Long memberId, Long bookId, Integer pageNo) throws Exception {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.BOOK_NOT_FOUND));

        ReadingMemo memo = readingMemoRepository.findByMemberAndBookAndPageNo(member, book, pageNo)
                .orElseThrow(() -> new CustomException(ReadingErrorCode.MEMO_NOT_FOUND));

        readingMemoRepository.delete(memo);
    }
}