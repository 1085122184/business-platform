package com.cjx.uibot.service;

import com.cjx.common.strategy.context.StrategyContext;

public interface UiBotService {
    void test(String name);

    void startProcess(String jsonString);

    void startProcess(StrategyContext context);
}
