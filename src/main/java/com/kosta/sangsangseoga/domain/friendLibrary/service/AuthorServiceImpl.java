package com.kosta.sangsangseoga.domain.friendLibrary.service;

import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.book.enums.BookStatus;
import com.kosta.sangsangseoga.domain.book.repository.BookRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.dto.AuthorListItemDto;
import com.kosta.sangsangseoga.domain.friendLibrary.dto.AuthorListResponseDto;
import com.kosta.sangsangseoga.domain.friendLibrary.exception.FriendLibraryErrorCode;
import com.kosta.sangsangseoga.domain.friendLibrary.repository.AuthorFollowRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.repository.AuthorRepository;
import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthorServiceImpl implements AuthorService {

    private static final BookStatus PUBLISHED = BookStatus.PUBLISHED;
    private static final List<String> VALID_SORTS = Arrays.asList("followers", "works");

    private final AuthorRepository authorRepository;
    private final AuthorFollowRepository authorFollowRepository;
    private final BookRepository bookRepository;

    /**
     * 작가 검색/목록 조회
     * - 작품이 없는 회원도 검색 결과에 포함
     * - keyword 미입력 시 전체 회원 목록, sort 기본값 followers
     */
    @Override
    public AuthorListResponseDto getAuthors(String keyword, String sort, int page, int size, Long memberId) throws Exception {
        String sortKey = (sort == null || sort.isBlank()) ? "followers" : sort;
        if (!VALID_SORTS.contains(sortKey)) {
            throw new CustomException(FriendLibraryErrorCode.INVALID_PARAMETER);
        }

        String keywordFilter = (keyword == null || keyword.isBlank()) ? null : keyword;

        PageRequest pageRequest = PageRequest.of(page - 1, size);
        Page<Member> authorPage = "works".equals(sortKey)
                ? authorRepository.findAuthorsByWorks(keywordFilter, pageRequest)
                : authorRepository.findAuthorsByFollowers(keywordFilter, pageRequest);

        List<AuthorListItemDto> items = new ArrayList<>();
        for (Member author : authorPage.getContent()) {
            long followerCount = authorFollowRepository.countByAuthor(author);
            long worksCount = bookRepository.countByMemberAndStatus(author, PUBLISHED);
            String representativeWork = bookRepository
                    .findTopByMemberAndStatusOrderByLikeCountDesc(author, PUBLISHED)
                    .map(Book::getTitle)
                    .orElse(null);
            boolean isFollowedByMe = memberId != null
                    && authorFollowRepository.existsByFollower_IdAndAuthor_Id(memberId, author.getId());

            items.add(AuthorListItemDto.builder()
                    .id(author.getId())
                    .nickname(author.getNickname())
                    .profileImageUrl(author.getProfileImageUrl())
                    .introduction(author.getIntroduction())
                    .followerCount(followerCount)
                    .worksCount(worksCount)
                    .representativeWork(representativeWork)
                    .isFollowedByMe(isFollowedByMe)
                    .build());
        }

        return AuthorListResponseDto.builder()
                .items(items)
                .totalCount(authorPage.getTotalElements())
                .page(page)
                .hasNext(authorPage.hasNext())
                .build();
    }
}
