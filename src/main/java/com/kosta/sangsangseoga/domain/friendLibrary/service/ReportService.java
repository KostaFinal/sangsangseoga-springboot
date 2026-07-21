package com.kosta.sangsangseoga.domain.friendLibrary.service;
 
import com.kosta.sangsangseoga.domain.friendLibrary.dto.ReportDto;
import com.kosta.sangsangseoga.domain.friendLibrary.dto.ReportRequestDto;
import com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportTargetType;

import java.util.List;

public interface ReportService {

    // 신고 등록 (책/댓글/작가 통합) - 201 응답
    ReportDto addReport(Long reporterId, ReportRequestDto request) throws Exception;

    // 내가 신고한 대상 ID 목록 조회 (targetType별) - 비로그인이면 빈 목록
    List<Long> getMyReportedTargetIds(Long reporterId, ReportTargetType targetType);
}
 