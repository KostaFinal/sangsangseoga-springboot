package com.kosta.sangsangseoga.domain.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI 이미지 생성 요청. Python(FastAPI) /api/ai/generate-image로 그대로 위임된다.")
public class AiGenerateImageRequestDto {

    @Schema(description = "이미지 생성용 프롬프트. createCoverPrompt/createImagePrompt(taskType) 결과의 coverPrompt/imagePrompt를 그대로 사용한다.")
    private String promptText;

    @Schema(description = "이미지 유형", allowableValues = {"COVER", "PAGE", "CHARACTER", "BACKGROUND"})
    private String imageType;

    @Schema(description = "본문 삽화(PAGE)일 때 페이지 번호. COVER 등은 생략 가능")
    private Integer pageNo;

    @Schema(description = "그림체/스타일 코드", allowableValues = {"PASTEL", "WATERCOLOR", "CUTE_3D", "PICTURE_BOOK"})
    private String style;

    @Schema(description = "이미지 비율", example = "3:4")
    private String aspectRatio;

    @Schema(description = "책 장르. FAIRY_TALE이면 아동 삽화용 안전/캐릭터 일관성 조건이 프롬프트에 추가로 붙는다.", allowableValues = {"NOVEL", "POEM", "ESSAY", "FAIRY_TALE", "NONFICTION"})
    private String bookType;

    @Schema(description = "캐릭터 일관성용 레퍼런스 이미지의 로컬 URL(예: 이미 생성된 표지의 /uploads/ai-images/... URL). "
            + "AiImageService가 이 URL로 로컬 파일을 읽어 referenceImageBase64로 채운 뒤 Python에 전달한다. React가 직접 base64를 보낼 필요는 없다.")
    private String referenceImageUrl;

    @Schema(description = "[내부용] referenceImageUrl로 읽어들인 이미지의 base64 데이터. AiImageService가 Python 호출 직전에 채운다. React가 직접 채우지 않는다.")
    private String referenceImageBase64;

    @Schema(description = "[내부용] referenceImageBase64의 MIME 타입. AiImageService가 Python 호출 직전에 채운다.")
    private String referenceImageMimeType;
}
