package com.cjx.decision.job;

import com.cjx.decision.event.CacheWarmUpEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 定时任务调度器
 *
 * @author cuijixu
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheWarmUpJob {

    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "0 0 6 * * ?")
//    @Scheduled(cron = "0 43 14 * * ?")
    public void executeWarmUp() {
        LocalDate today = LocalDate.now();
        log.info("========== 开始发布全局大屏缓存预热指令, Date: {} ==========", today);

        // 发布预热事件，底层的预热引擎会自动接管
        eventPublisher.publishEvent(new CacheWarmUpEvent(this, today));

        log.info("========== 全局大屏缓存预热指令发布完毕 ==========");
    }
}
