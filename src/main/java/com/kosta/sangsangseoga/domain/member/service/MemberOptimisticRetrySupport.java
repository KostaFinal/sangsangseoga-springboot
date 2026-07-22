package com.kosta.sangsangseoga.domain.member.service;

import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.function.Consumer;

/**
 * Member 저장 시 낙관적 락 충돌(같은 회원 행을 다른 요청이 거의 동시에 저장)이 나면, 최신 값을 다시
 * 읽어 원래 하려던 변경을 재적용하고 다시 저장한다. reconcileIfExpired처럼 "충돌 나면 포기해도 되는"
 * 로직과 달리, 프로필/뷰어설정/구독 상태 변경처럼 사용자가 명시적으로 요청한 변경은 충돌 났다고
 * 조용히 포기하면 그 값이 사라지는 셈이라 반드시 재적용해서 저장까지 성공시켜야 한다.
 */
@Component
@RequiredArgsConstructor
public class MemberOptimisticRetrySupport {

    private static final int MAX_ATTEMPTS = 3;

    private final MemberRepository memberRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * @param memberId 대상 회원 ID
     * @param mutator  최신 Member에 적용할 변경 로직. 충돌로 재시도할 때마다 새로 읽은 엔티티에 다시 적용된다.
     * @return 저장에 성공한 최신 Member
     */
    public Member saveWithRetry(Long memberId, Consumer<Member> mutator) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));
        mutator.accept(member);

        for (int attempt = 1; ; attempt++) {
            try {
                return memberRepository.saveAndFlush(member);
            } catch (ObjectOptimisticLockingFailureException e) {
                if (attempt >= MAX_ATTEMPTS) {
                    throw e;
                }
                entityManager.refresh(member);
                mutator.accept(member);
            }
        }
    }
}
