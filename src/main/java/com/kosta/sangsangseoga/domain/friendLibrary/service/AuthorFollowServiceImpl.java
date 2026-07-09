package com.kosta.sangsangseoga.domain.friendLibrary.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.book.enums.BookStatus;
import com.kosta.sangsangseoga.domain.book.repository.BookRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.dto.AuthorFollowDto;
import com.kosta.sangsangseoga.domain.friendLibrary.dto.AuthorListItemDto;
import com.kosta.sangsangseoga.domain.friendLibrary.dto.AuthorListResponseDto;
import com.kosta.sangsangseoga.domain.friendLibrary.entity.AuthorFollow;
import com.kosta.sangsangseoga.domain.friendLibrary.exception.FriendLibraryErrorCode;
import com.kosta.sangsangseoga.domain.friendLibrary.repository.AuthorFollowRepository;
import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthorFollowServiceImpl implements AuthorFollowService {

    private final AuthorFollowRepository authorFollowRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;

    /**
     * 팔로우
     * - 본인 팔로우 방지
     * - 이미 팔로우한 경우 예외 발생
     * - 팔로우 후 authorId, isFollowedByMe, followerCount 반환
     */
    @Override
    public AuthorFollowDto follow(Long followerId, Long authorId) throws Exception {
        // 본인 팔로우 방지
        if (followerId.equals(authorId)) {
            throw new CustomException(FriendLibraryErrorCode.SELF_FOLLOW_NOT_ALLOWED);
        }

        Member follower = memberRepository.findById(followerId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        Member author = memberRepository.findById(authorId)
                .orElseThrow(() -> new CustomException(FriendLibraryErrorCode.AUTHOR_NOT_FOUND));

        // 중복 팔로우 방지
        if (authorFollowRepository.existsByFollowerAndAuthor(follower, author)) {
            throw new CustomException(FriendLibraryErrorCode.FOLLOW_ALREADY_EXISTS);
        }

        authorFollowRepository.save(AuthorFollow.builder()
                .follower(follower)
                .author(author)
                .build());

        // 팔로우 후 총 팔로워 수 집계
        Long followerCount = authorFollowRepository.countByAuthor(author);

        return AuthorFollowDto.builder()
                .authorId(authorId)
                .isFollowedByMe(true)
                .followerCount(followerCount)
                .build();
    }

    /**
     * 언팔로우
     * - 팔로우하지 않은 경우 예외 발생
     * - DELETE 204 응답이라 반환값 없음
     */
    @Override
    public void unfollow(Long followerId, Long authorId) throws Exception {
        Member follower = memberRepository.findById(followerId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

        Member author = memberRepository.findById(authorId)
                .orElseThrow(() -> new CustomException(FriendLibraryErrorCode.AUTHOR_NOT_FOUND));

        // 팔로우 row 조회
        AuthorFollow authorFollow = authorFollowRepository.findByFollowerAndAuthor(follower, author)
                .orElseThrow(() -> new CustomException(FriendLibraryErrorCode.FOLLOW_NOT_FOUND));

        authorFollowRepository.delete(authorFollow);
    }
    
    @Override
    @Transactional(readOnly = true)
    public AuthorListResponseDto getMyFollowedAuthors(Long memberId, int page, int size) throws Exception {
    	Page<AuthorFollow> authorFollowPage =
    	        authorFollowRepository.findByFollower_IdOrderByCreatedAtDesc(
    	                memberId,
    	                PageRequest.of(page - 1, size)
    	        );

    	List<AuthorListItemDto> items = authorFollowPage
    	        .getContent()
    	        .stream()
    	        .map(authorFollow -> {
                    Member author = authorFollow.getAuthor();

                    long followerCount = authorFollowRepository.countByAuthor(author);
                    long worksCount = bookRepository.countByMemberAndStatus(author, BookStatus.PUBLISHED);
                    String representativeWork = bookRepository
                            .findTopByMemberAndStatusOrderByLikeCountDesc(author, BookStatus.PUBLISHED)
                            .map(Book::getTitle)
                            .orElse(null);

                    return AuthorListItemDto.builder()
                            .id(author.getId())
                            .nickname(author.getNickname())
                            .profileImageUrl(author.getProfileImageUrl())
                            .introduction(author.getIntroduction())
                            .followerCount(followerCount)
                            .worksCount(worksCount)
                            .representativeWork(representativeWork)
                            .isFollowedByMe(true)
                            .build();
                })
                .collect(Collectors.toList());

    	return AuthorListResponseDto.builder()
    	        .items(items)
    	        .totalCount(authorFollowPage.getTotalElements())
    	        .page(authorFollowPage.getNumber() + 1)
    	        .hasNext(authorFollowPage.hasNext())
    	        .build();
    }
}