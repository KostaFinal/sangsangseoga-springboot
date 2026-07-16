package com.kosta.sangsangseoga.domain.book.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookPublishRequestDto {

    private String bookType;          // "FAIRY_TALE" | "NOVEL" | "POEM" | "ESSAY"
    private String authorAgeGroup;    // "PRESCHOOL" | "LOWER_ELEMENTARY" | "UPPER_ELEMENTARY" | "TEEN" | "ADULT"
    private String readerAgeGroup;    // "PRESCHOOL" | "LOWER_ELEMENTARY" | "UPPER_ELEMENTARY" | "TEEN" | "ADULT"
    private String creationMode;      // "FREE" | "MIXED" | "CHOICE" | "ANSWER" | "GUIDED"
    private String title;
    private String description;       // 책 소개 (선택)
    private String confirmedSettings; // 설정 정보를 JSON 문자열로 직렬화해서 저장
    private String coverImageUrl;     // 표지 이미지 URL (AI 생성 결과)
    private List<PageRequest> pages;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageRequest {
        private Integer pageNo;
        private String title;
        private String titleEn;         // 소제목 영어 번역 (없으면 null/빈 문자열)
        private String contentType;     // "TEXT" | "IMAGE" | "TEXT_IMAGE"
        private String contentTextKo;   // 본문 텍스트
        private String contentTextEn;   // 본문 텍스트 영어 번역 (없으면 null/빈 문자열)
        private String imageUrl;        // AI 생성 이미지 URL (없으면 null)
    }
}
