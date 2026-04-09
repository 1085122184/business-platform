package com.cjx.common.ai.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 向量存储配置
 *
 * <p>默认注册内存版 SimpleVectorStore（随应用重启丢失，适合开发/演示）。
 * 生产环境引入对应 starter（如 spring-ai-starter-vector-store-pgvector）后，
 * Spring Boot 自动装配会创建对应 VectorStore Bean，本 Bean 因
 * {@code @ConditionalOnMissingBean} 自动退让。
 */
@Configuration
public class VectorStoreConfig {

    @Bean
    @ConditionalOnMissingBean(VectorStore.class)
    public VectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
