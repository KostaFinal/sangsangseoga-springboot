package com.kosta.sangsangseoga.domain.friendLibrary.service;

import com.kosta.sangsangseoga.domain.account.entity.Member;
import com.kosta.sangsangseoga.domain.account.repository.MemberRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.dto.AuthorFollowDto;
import com.kosta.sangsangseoga.domain.friendLibrary.entity.AuthorFollow;
import com.kosta.sangsangseoga.domain.friendLibrary.exception.FriendLibraryErrorCode;
import com.kosta.sangsangseoga.domain.friendLibrary.repository.AuthorFollowRepository;
import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthorFollowServiceImpl implements AuthorFollowService {

    private final AuthorFollowRepository authorFollowRepository;
    private final MemberRepository memberRepository;

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
}