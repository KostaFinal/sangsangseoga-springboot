package com.kosta.sangsangseoga.global.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * DB 트랜잭션 커밋 이후에만 실행돼야 하는 부수 작업(Redis 등 롤백 불가능한 외부 시스템 쓰기)을 감싼다.
 * 트랜잭션이 롤백되면 이 이벤트의 task는 아예 실행되지 않는다.
 */
@Getter
public class AfterCommitTask extends ApplicationEvent {

    private final Runnable task;

    public AfterCommitTask(Object source, Runnable task) {
        super(source);
        this.task = task;
    }
}
