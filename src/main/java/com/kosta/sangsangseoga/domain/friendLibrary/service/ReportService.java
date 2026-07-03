package com.kosta.sangsangseoga.domain.friendLibrary.service;
 
import com.kosta.sangsangseoga.domain.friendLibrary.dto.ReportDto;
import com.kosta.sangsangseoga.domain.friendLibrary.dto.ReportRequestDto;
 
public interface ReportService {
 
    // 신고 등록 (책/댓글/작가 통합) - 201 응답
    ReportDto addReport(Long reporterId, ReportRequestDto request) throws Exception;
}
 