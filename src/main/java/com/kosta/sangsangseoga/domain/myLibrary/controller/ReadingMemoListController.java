package com.kosta.sangsangseoga.domain.myLibrary.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kosta.sangsangseoga.domain.myLibrary.dto.ReadingMemoDto;
import com.kosta.sangsangseoga.domain.myLibrary.service.ReadingMemoService;
import com.kosta.sangsangseoga.global.common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/books/{bookId}/memos")
@RequiredArgsConstructor
public class ReadingMemoListController {
	private final ReadingMemoService readingMemoService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReadingMemoDto>>> getMemosByBook(
            @PathVariable Long bookId,
            @AuthenticationPrincipal Long memberId
    ) throws Exception {

        List<ReadingMemoDto> result =
                readingMemoService.getMemosByBook(memberId, bookId);

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}

