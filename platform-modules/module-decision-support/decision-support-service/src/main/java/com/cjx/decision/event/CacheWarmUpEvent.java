package com.cjx.decision.event;

import org.springframework.context.ApplicationEvent;

import java.time.LocalDate;

/**
 * 全局缓存预热广播事件
 * * @author cuijixu
 */
public class CacheWarmUpEvent extends ApplicationEvent {
    private final LocalDate targetDate;

    public CacheWarmUpEvent(Object source, LocalDate targetDate) {
        super(source);
        this.targetDate = targetDate;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }
}
