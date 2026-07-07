package com.kosta.sangsangseoga.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.kosta.sangsangseoga.domain.book.enums.AgeGroup;
import com.kosta.sangsangseoga.domain.book.enums.BookType;
import com.kosta.sangsangseoga.domain.book.enums.CreationMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiGenerateRequestDto {

    private Long bookId;
    private BookType bookType;
    private CreationMode creationMode;
    private AgeGroup authorAgeGroup;
    private AgeGroup readerAgeGroup; // optional

    @JsonAlias("taskType") // Python 스키마(taskType)로 직접 테스트하는 Postman 요청도 허용
    private String stage;
    private String message;
    private Integer pageNo;
    private Map<String, Object> context;

    // Postman 등으로 평평한 구조로 직접 테스트할 때 사용하는 선택 필드.
    // React 정식 흐름에서는 context.draft.setting에 담겨 오고, 이 값들은 비어 있다.
    private String prompt;
    private String storySeed;
    private String protagonistName;
    private String backgroundPlace;
    private String problem;

    // Postman이 Python이 기대하는 구조(taskType/draft/extra가 최상위)를 그대로 보낼 때 사용하는 선택 필드.
    // React 정식 흐름에서는 context.draft / context.extra에 담겨 오고, 이 값들은 비어 있다.
    private Map<String, Object> draft;
    private Map<String, Object> extra;
}
