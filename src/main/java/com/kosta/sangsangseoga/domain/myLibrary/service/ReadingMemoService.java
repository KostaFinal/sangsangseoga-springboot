package com.kosta.sangsangseoga.domain.myLibrary.service;
import java.math.BigDecimal;
import com.kosta.sangsangseoga.domain.myLibrary.dto.ReadingMemoDto;
 
public interface ReadingMemoService {
 
    // 메모 조회 - 없으면 null 반환
    ReadingMemoDto getMemo(Long memberId, Long bookId, Integer pageNo) throws Exception;
 
    // 메모 작성 - 201 응답
    ReadingMemoDto addMemo(Long memberId, Long bookId, Integer pageNo, String content, BigDecimal posX, BigDecimal posY) throws Exception;
 
    // 메모 수정 - 200 응답
    ReadingMemoDto updateMemo(Long memberId, Long bookId, Integer pageNo, String content, BigDecimal posX, BigDecimal posY) throws Exception;
 
    // 메모 삭제 - 204 응답
    void deleteMemo(Long memberId, Long bookId, Integer pageNo) throws Exception;
}
