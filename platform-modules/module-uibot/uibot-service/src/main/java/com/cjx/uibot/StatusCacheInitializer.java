package com.cjx.uibot;

import com.cjx.uibot.entity.UiBotProcessStatus;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class StatusCacheInitializer {
    @Autowired
    @Qualifier("statusCache")
    private Map<String, UiBotProcessStatus> statusCache;

    @PostConstruct
    public void initialize(){
        UiBotProcessStatus uiBotProcessStatus = new UiBotProcessStatus();
        uiBotProcessStatus.setProcessStatus("0");
        statusCache.put("status",uiBotProcessStatus);
    }
}
