package com.kosta.sangsangseoga.domain.myLibrary.dto;

import java.time.LocalDate;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReadingPlanRequestDto {
	
	 @NotNull(message = "책 ID는 필수입니다.")
	    private Long bookId;

	    @NotNull(message = "계획 날짜는 필수입니다.")
	    private LocalDate planDate;

	    @Min(value = 1, message = "목표 페이지는 1 이상이어야 합니다.")
	    private Integer targetPage;

	    @Size(max = 500, message = "메모는 500자 이하로 입력해 주세요.")
	    private String memo;
}
