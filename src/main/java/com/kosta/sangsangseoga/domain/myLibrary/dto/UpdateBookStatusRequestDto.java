package com.kosta.sangsangseoga.domain.myLibrary.dto;

import lombok.Data;

@Data
public class UpdateBookStatusRequestDto {
	private String status; // PUBLISHED 또는 HIDDEN
}
