package com.cjx.common.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ai.common")
public class AiProperties {

    private boolean enabled  = true;
    private String  provider = "openai";
    private Chat    chat     = new Chat();
    private Rag     rag      = new Rag();
    private Log     log      = new Log();

    @Data
    public static class Chat {
        private String  systemPrompt     = "You are a helpful AI assistant. Please respond in the same language as the user.";
        private boolean memoryEnabled    = true;
        private int     memoryWindowSize = 10;
    }

    @Data
    public static class Rag {
        private int    topK                = 5;
        private double similarityThreshold = 0.7;
        private int    chunkSize           = 800;
        private int    chunkOverlap        = 100;
    }

    @Data
    public static class Log {
        private boolean logRequest  = false;
        private boolean logResponse = false;
    }
}
