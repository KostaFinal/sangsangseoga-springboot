package com.kosta.sangsangseoga.domain.book.dto;

import com.kosta.sangsangseoga.domain.book.entity.BookPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookPageDto {

    private Long id;
    private Integer pageNo;
    private String title;
    private String titleEn;
    private BookPage.ContentType contentType;
    private String contentTextKo;
    private String contentTextEn;
    private String imageUrl;
}