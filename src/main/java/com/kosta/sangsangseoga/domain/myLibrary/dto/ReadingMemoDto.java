package com.kosta.sangsangseoga.domain.myLibrary.dto;
 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadingMemoDto {
 
    private Long id;
    private Long bookId;
    private Integer pageNo;
    private String content;
    private BigDecimal posX;
    private BigDecimal posY;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}