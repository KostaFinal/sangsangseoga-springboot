package com.kosta.sangsangseoga.domain.friendLibrary.dto;
 
import com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportReason;
import com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRequestDto {
 
    private ReportTargetType targetType;
    private Long targetId;
    private ReportReason reason;
    private String reasonDetail; // 상세 설명 (255자 이하, 선택)
}
 