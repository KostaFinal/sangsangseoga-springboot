package com.kosta.sangsangseoga.domain.myLibrary.dto;


import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import lombok.Data;

@Data
public class UpdateBookStatusRequestDto {

	
	@NotBlank(message = "책 상태는 필수입니다.")
    @Pattern(
        regexp = "PUBLIC|HIDDEN",
        message = "책 상태는 PUBLIC 또는 HIDDEN만 가능합니다."
    )
	private String status; // PUBLIC 또는 HIDDEN
}
