package com.kosta.sangsangseoga.domain.ai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AiService {
	// TODO: 텍스트/이미지 생성 메서드 구현 시 UsageService와 연동 필요.
	//  - PREMIUM 회원: 생성 전 UsageService.consumeText()/consumeImage() 호출(잔여량 0이면 예외)
	//  - FREE 회원: UsageService.canGenerateFreeTrialText()/canGenerateFreeTrialImage() 체크
	//    (페이지 수 제한과 별개로, 재생성 남용으로 원가만 나가는 것을 막는 호출 횟수 상한)
}
