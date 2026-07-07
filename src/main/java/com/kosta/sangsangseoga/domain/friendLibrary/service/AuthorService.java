package com.kosta.sangsangseoga.domain.friendLibrary.service;

import com.kosta.sangsangseoga.domain.friendLibrary.dto.AuthorListResponseDto;

public interface AuthorService {

    // 작가 검색/목록 조회 - keyword/sort/page/size
    AuthorListResponseDto getAuthors(String keyword, String sort, int page, int size) throws Exception;
}
