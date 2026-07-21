package com.kosta.sangsangseoga.domain.friendLibrary.dto;
 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.time.LocalDateTime;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentUpdateDto {
 
    private Long id;
    private String content;
    private LocalDateTime updatedAt;
}