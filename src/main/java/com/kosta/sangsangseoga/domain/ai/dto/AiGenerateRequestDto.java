package com.kosta.sangsangseoga.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.kosta.sangsangseoga.domain.book.enums.AgeGroup;
import com.kosta.sangsangseoga.domain.book.enums.BookType;
import com.kosta.sangsangseoga.domain.book.enums.CreationMode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@Schema(description = "AI 생성 요청. React 정식 흐름은 bookId/bookType/creationMode/authorAgeGroup/readerAgeGroup/stage(taskType)와 "
        + "context: { draft, extra }만 채워서 보낸다. prompt/storySeed/protagonistName/backgroundPlace/problem/draft/extra(최상위)는 "
        + "Postman 등으로 평평한 구조를 직접 테스트할 때만 쓰는 선택 필드이며, React는 사용하지 않는다.")
public class AiGenerateRequestDto {

    @Schema(description = "콘텐츠가 속한 book의 ID")
    private Long bookId;

    @Schema(description = "콘텐츠 유형")
    private BookType bookType;

    @Schema(description = "작성 방식(선택형/응답형/자유형/혼합형)")
    private CreationMode creationMode;

    @Schema(description = "작성자 연령대")
    private AgeGroup authorAgeGroup;

    @Schema(description = "독자 연령대(선택)")
    private AgeGroup readerAgeGroup; // optional

    @Schema(
            description = "Python(FastAPI)이 기대하는 taskType. 어떤 작업을 수행할지 결정한다.",
            example = "WRITE_PAGE",
            allowableValues = {
                    "NORMALIZE_SETTING", "COLLECT_SETTING", "CREATE_SETTING_OPTIONS",
                    "CREATE_PAGE_PLAN", "WRITE_PAGE", "REWRITE_PAGE",
                    "CREATE_SCENE_PLAN", "WRITE_SCENE",
                    "CREATE_IMAGE_PROMPT", "CREATE_COVER_PROMPT"
            }
    )
    @JsonAlias("taskType") // Python 스키마(taskType)로 직접 테스트하는 Postman 요청도 허용
    private String stage;

    @Schema(description = "[레거시] 사용자 메시지. React 정식 흐름에서는 context.extra.userMessage를 대신 사용한다.")
    private String message;

    @Schema(description = "현재 작업 중인 페이지 번호(선택)")
    private Integer pageNo;

    @Schema(description = "React 정식 흐름의 실제 페이로드. { draft: {...}, extra: {...} } 형태로 중첩해서 보낸다.")
    private Map<String, Object> context;

    // Postman 등으로 평평한 구조로 직접 테스트할 때 사용하는 선택 필드.
    // React 정식 흐름에서는 context.draft.setting에 담겨 오고, 이 값들은 비어 있다.
    @Schema(description = "[테스트 전용] Postman 등으로 평평하게 테스트할 때만 사용. React는 사용하지 않는다.")
    private String prompt;
    @Schema(description = "[테스트 전용] 위와 동일.")
    private String storySeed;
    @Schema(description = "[테스트 전용] 위와 동일.")
    private String protagonistName;
    @Schema(description = "[테스트 전용] 위와 동일.")
    private String backgroundPlace;
    @Schema(description = "[테스트 전용] 위와 동일.")
    private String problem;

    // Postman이 Python이 기대하는 구조(taskType/draft/extra가 최상위)를 그대로 보낼 때 사용하는 선택 필드.
    // React 정식 흐름에서는 context.draft / context.extra에 담겨 오고, 이 값들은 비어 있다.
    @Schema(description = "[테스트 전용] Python 스키마를 그대로 최상위에 보낼 때 사용. React는 context.draft를 사용한다.")
    private Map<String, Object> draft;
    @Schema(description = "[테스트 전용] 위와 동일. React는 context.extra를 사용한다.")
    private Map<String, Object> extra;
}
