package com.kosta.sangsangseoga.domain.subscription.dto;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class UpdateBookTagsRequestDto {
	@NotNull(message = "태그 목록은 필수입니다.")
    @Size(max = 10, message = "태그는 최대 10개까지 등록할 수 있습니다.")
    private List<String> tags;
}
