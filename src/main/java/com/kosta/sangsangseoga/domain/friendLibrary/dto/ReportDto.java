package com.kosta.sangsangseoga.domain.friendLibrary.dto;
 
import com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportStatus;
import com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.time.LocalDateTime;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDto {
 
    private Long id;
    private ReportTargetType targetType;
    private Long targetId;
    private ReportStatus status;
    private LocalDateTime createdAt;
}