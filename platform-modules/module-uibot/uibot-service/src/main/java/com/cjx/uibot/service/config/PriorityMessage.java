package com.cjx.uibot.service.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriorityMessage {
    private String id;
    private String content;
    private int priority; // 优先级，数字越大优先级越高
    private long timestamp;

    public PriorityMessage(String content, int priority) {
        this.id = UUID.randomUUID().toString();
        this.content = content;
        this.priority = priority;
        this.timestamp = System.currentTimeMillis();
    }
}