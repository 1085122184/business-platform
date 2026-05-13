package com.cjx.uibot.service.impl;

import com.cjx.common.core.exception.BusinessException;
import com.cjx.common.core.utils.OkHttpUtil;
import com.cjx.common.strategy.annotation.Strategy;
import com.cjx.common.strategy.context.StrategyContext;
import com.cjx.uibot.config.ConfigAccessor;
import com.cjx.uibot.service.UiBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author cjx
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UiBotServiceImpl implements UiBotService {

    private final OkHttpUtil okHttpUtil;
    private final ConfigAccessor configAccessor;
    @Override
    public void test(String name) {
        System.out.println(name);
    }

    @Override
    public void startProcess(String triggerName) {
        log.info("123====执行任务");
        try {
            okHttpUtil.get(configAccessor.getStartPath()+triggerName);
        } catch (IOException e) {
            throw new BusinessException(e.getMessage(),e);
        }
    }

    @Override
    @Strategy(value = "uiBot-start", description = "开启流程机器人")
    public void startProcess(StrategyContext context) {
        log.info("123====uiBot-start");
        try {
            String triggerName = String.valueOf(context.getParams().get("triggerName"));
            okHttpUtil.get(configAccessor.getStartPath()+triggerName);
        } catch (IOException e) {
            throw new BusinessException(e.getMessage(),e);
        }
    }
}
