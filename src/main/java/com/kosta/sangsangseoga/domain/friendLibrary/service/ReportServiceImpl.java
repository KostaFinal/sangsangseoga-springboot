package com.kosta.sangsangseoga.domain.friendLibrary.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.book.repository.BookRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.dto.ReportDto;
import com.kosta.sangsangseoga.domain.friendLibrary.dto.ReportRequestDto;
import com.kosta.sangsangseoga.domain.friendLibrary.entity.Report;
import com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportStatus;
import com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportTargetType;
import com.kosta.sangsangseoga.domain.friendLibrary.exception.FriendLibraryErrorCode;
import com.kosta.sangsangseoga.domain.friendLibrary.repository.CommentRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.repository.ReportRepository;
import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
import com.kosta.sangsangseoga.domain.notification.service.NotificationService;
import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;

    /**
     * 신고 등록 (책/댓글/작가 통합)
     * - targetType에 따라 대상 존재 여부 검증
     * - 동일 대상 중복 신고 방지
     */
    @Override
    public ReportDto addReport(Long reporterId, ReportRequestDto request) throws Exception {
        // 필수 파라미터 검증
        if (request.getTargetType() == null || request.getTargetId() == null || request.getReason() == null) {
            throw new CustomException(FriendLibraryErrorCode.MISSING_PARAMETER);
        }
        

        Member reporter = memberRepository.findById(reporterId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        // 중복 신고 방지
        if (reportRepository.existsByReporterAndTargetTypeAndTargetId(reporter, request.getTargetType(), request.getTargetId())) {
            throw new CustomException(FriendLibraryErrorCode.REPORT_ALREADY_EXISTS);
        }

        // targetType에 따라 대상 존재 여부 검증
        validateTarget(request.getTargetType(), request.getTargetId());

        Report report = reportRepository.save(Report.builder()
                .reporter(reporter)
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .reason(request.getReason())
                .reasonDetail(request.getReasonDetail())
                .status(ReportStatus.PENDING)
                .build());

        if (request.getTargetType() == ReportTargetType.BOOK) {
            notifyBookAuthor(request.getTargetId());
        }

        return ReportDto.builder()
                .id(report.getId())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }

    /**
     * 내가 신고한 대상 ID 목록 조회
     * - 비로그인(reporterId=null)이면 빈 목록
     */
    @Override
    public List<Long> getMyReportedTargetIds(Long reporterId, ReportTargetType targetType) {
        if (reporterId == null) {
            return List.of();
        }
        return reportRepository.findTargetIdsByReporterIdAndTargetType(reporterId, targetType);
    }

    /** 내 책이 신고당했음을 작가 본인에게 알린다. 대상 존재는 validateTarget에서 이미 확인했다. */
    private void notifyBookAuthor(Long bookId) {
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) {
            return;
        }
        notificationService.notify(book.getMember(),
                String.format("회원님의 책 '%s'이(가) 신고되었습니다.", book.getTitle()));
    }

    /**
     * targetType에 따라 신고 대상 존재 여부 검증
     * - BOOK: book 테이블에서 조회
     * - COMMENT: comment 테이블에서 조회
     * - AUTHOR: member 테이블에서 조회
     */
    private void validateTarget(ReportTargetType targetType, Long targetId) throws Exception {
        switch (targetType) {
            case BOOK:
                bookRepository.findById(targetId)
                        .orElseThrow(() -> new CustomException(FriendLibraryErrorCode.TARGET_NOT_FOUND));
                break;
            case COMMENT:
                commentRepository.findByIdAndIsDeletedFalse(targetId)
                        .orElseThrow(() -> new CustomException(FriendLibraryErrorCode.TARGET_NOT_FOUND));
                break;
            
        }
    }
}