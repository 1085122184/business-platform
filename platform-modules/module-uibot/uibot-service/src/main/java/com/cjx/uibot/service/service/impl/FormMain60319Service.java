package com.cjx.uibot.service.service.impl;

import com.cjx.common.strategy.annotation.Strategy;
import com.cjx.common.strategy.context.StrategyContext;
import com.cjx.uibot.service.entity.oa.FormMain60319;
import com.cjx.uibot.service.repository.oa.FormMain60319Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author cjx
 */
@Service
@RequiredArgsConstructor
public class FormMain60319Service {
    private final FormMain60319Repository repository;

    @Strategy(value = "formmain60319-get", description = "获取formmain60319表数据")
    public FormMain60319 getFormMain60319(StrategyContext context){
        Optional<FormMain60319> optional = repository.findByIdEager(Long.parseLong(context.getParams().get("tableId").toString()));
        return optional.get();
    }

}
