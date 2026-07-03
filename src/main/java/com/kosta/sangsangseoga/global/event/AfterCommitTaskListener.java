package com.kosta.sangsangseoga.global.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class AfterCommitTaskListener {

    /**
     * 트랜잭션이 성공적으로 커밋된 뒤에만 실행된다. 롤백되면 호출 자체가 되지 않는다.
     * 여기서 실패해도 이미 커밋된 DB 트랜잭션에는 영향이 없으므로 로그만 남긴다.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AfterCommitTask event) {
        try {
            event.getTask().run();
        } catch (Exception e) {
            log.error("트랜잭션 커밋 후 처리 중 오류 발생", e);
        }
    }
}
