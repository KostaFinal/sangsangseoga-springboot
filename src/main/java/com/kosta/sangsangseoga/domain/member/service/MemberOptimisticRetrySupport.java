package com.kosta.sangsangseoga.domain.member.service;

import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Consumer;

/**
 * Member 저장 시 낙관적 락 충돌(같은 회원 행을 다른 요청이 거의 동시에 저장)이 나면, 최신 값을 다시
 * 읽어 원래 하려던 변경을 재적용하고 다시 저장한다. reconcileIfExpired처럼 "충돌 나면 포기해도 되는"
 * 로직과 달리, 프로필/뷰어설정/구독 상태 변경처럼 사용자가 명시적으로 요청한 변경은 충돌 났다고
 * 조용히 포기하면 그 값이 사라지는 셈이라 반드시 재적용해서 저장까지 성공시켜야 한다.
 *
 * 호출부는 대부분 이미 클래스 레벨 @Transactional 메서드 안에서 이걸 부른다. 그 트랜잭션을 그대로
 * 쓰면서 재시도하면, 레포지토리 저장 자체가 트랜잭션 경계라 첫 실패 시점에 예외가 그 경계를 넘으며
 * 물리 트랜잭션이 rollback-only로 마킹된다 - 이후 재시도가 saveAndFlush에서 예외 없이 "성공"해도
 * 커밋 시점에 전체가 조용히 롤백된다. 그래서 시도마다 REQUIRES_NEW로 완전히 새 물리 트랜잭션을 열어
 * 호출부 트랜잭션 상태와 분리하고, 매번 findById로 최신 행을 다시 읽는다.
 */
@Component
public class MemberOptimisticRetrySupport {

    private static final int MAX_ATTEMPTS = 3;

    private final MemberRepository memberRepository;
    private final TransactionTemplate requiresNewTransactionTemplate;

    public MemberOptimisticRetrySupport(MemberRepository memberRepository, PlatformTransactionManager transactionManager) {
        this.memberRepository = memberRepository;
        this.requiresNewTransactionTemplate = new TransactionTemplate(transactionManager);
        this.requiresNewTransactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /**
     * @param memberId 대상 회원 ID
     * @param mutator  최신 Member에 적용할 변경 로직. 시도마다 새로 읽은 엔티티에 다시 적용되므로,
     *                 재시도 시점까지도 유효해야 하는 전제 조건(중복 검사, 상태 검증 등)이 있다면
     *                 이 안에서 직접 검사하고 위반 시 예외를 던져야 한다. 바깥에서 한 번만 검사하면
     *                 재시도 사이에 바뀐 상태를 놓친다.
     * @return 저장에 성공한 최신 Member
     */
    public Member saveWithRetry(Long memberId, Consumer<Member> mutator) {
        ObjectOptimisticLockingFailureException lastFailure = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return requiresNewTransactionTemplate.execute(status -> {
                    Member member = memberRepository.findById(memberId)
                            .orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));
                    mutator.accept(member);
                    return memberRepository.saveAndFlush(member);
                });
            } catch (ObjectOptimisticLockingFailureException e) {
                lastFailure = e;
            }
        }

        throw lastFailure;
    }
}
