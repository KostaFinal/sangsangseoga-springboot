package com.kosta.sangsangseoga.domain.friendLibrary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorListItemDto {

    private Long id;
    private String nickname;
    private String profileImageUrl;
    private String introduction;
    private Long followerCount;
    private Long worksCount;
    private String representativeWork;
    private Boolean isFollowedByMe;
}
