package com.cjx.common.ai.service;

import com.cjx.common.ai.exception.AiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 向量嵌入服务（Spring AI 1.1.3）
 *
 * <p>提供文本 → 向量的转换能力，支持单条/批量嵌入及余弦相似度计算。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AiEmbeddingService {

    private final EmbeddingModel embeddingModel;

    /** 单条文本嵌入 */
    public float[] embed(String text) {
        try {
            return embeddingModel.embed(text);
        } catch (Exception e) {
            log.error("[AiEmbeddingService] 嵌入失败: {}", e.getMessage(), e);
            throw new AiException("嵌入失败: " + e.getMessage(), e);
        }
    }

    /** 批量文本嵌入 */
    public EmbeddingResponse embedBatch(List<String> texts) {
        try {
            return embeddingModel.call(new EmbeddingRequest(texts, null));
        } catch (Exception e) {
            log.error("[AiEmbeddingService] 批量嵌入失败: {}", e.getMessage(), e);
            throw new AiException("批量嵌入失败: " + e.getMessage(), e);
        }
    }

    /** Document 嵌入（RAG 场景） */
    public float[] embedDocument(Document document) {
        return embed(document.getFormattedContent());
    }

    /**
     * 余弦相似度（-1 ~ 1，值越大越相似）
     */
    public double cosineSimilarity(float[] v1, float[] v2) {
        if (v1.length != v2.length) {
            throw new IllegalArgumentException("向量维度不一致");
        }
        double dot = 0, n1 = 0, n2 = 0;
        for (int i = 0; i < v1.length; i++) {
            dot += v1[i] * v2[i];
            n1  += v1[i] * v1[i];
            n2  += v2[i] * v2[i];
        }
        double denom = Math.sqrt(n1) * Math.sqrt(n2);
        return denom == 0 ? 0 : dot / denom;
    }

    /** 获取向量维度 */
    public int getDimensions() {
        return embeddingModel.dimensions();
    }
}
