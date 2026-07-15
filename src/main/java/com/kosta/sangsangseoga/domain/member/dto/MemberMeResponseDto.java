package com.kosta.sangsangseoga.domain.member.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberMeResponseDto {
	private Long memberId;
	private String nickname;
	private String profileImageUrl;
	private String introduction;
}
