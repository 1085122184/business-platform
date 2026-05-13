package com.cjx.common.ai.service;

import com.cjx.common.ai.config.AiProperties;
import com.cjx.common.ai.exception.AiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI RAG（检索增强生成）服务（Spring AI 1.1.3）
 *
 * <p>{@code QuestionAnswerAdvisor} 包路径（1.1.x）：
 * {@code org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor}
 * 已包含在 spring-ai-core 中，由 spring-ai-starter-model-openai 传递引入，无需额外依赖。
 *
 * <pre>
 * // 1. 文档入库
 * aiRagService.ingestText("公司年假政策：每年15天...", Map.of("source", "hr-policy"));
 *
 * // 2. 知识库问答
 * String answer = aiRagService.ragChat("年假有多少天？");
 * </pre>
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AiRagService {

    private final ChatClient   chatClient;
    private final VectorStore  vectorStore;
    private final AiProperties aiProperties;

    // ── 文档入库 ──────────────────────────────────────────────────────────────

    public void ingestText(String text, Map<String, Object> metadata) {
        List<Document> chunks = split(List.of(new Document(text, metadata)));
        vectorStore.add(chunks);
        log.info("[AiRagService] 文本入库完成，分片数={}", chunks.size());
    }

    public void ingestPdf(Resource pdfResource) {
        try {
            List<Document> chunks = split(new PagePdfDocumentReader(pdfResource).get());
            vectorStore.add(chunks);
            log.info("[AiRagService] PDF 入库完成，file={}, 分片数={}",
                    pdfResource.getFilename(), chunks.size());
        } catch (Exception e) {
            log.error("[AiRagService] PDF 入库失败: {}", e.getMessage(), e);
            throw new AiException("PDF 入库失败: " + e.getMessage(), e);
        }
    }

    public void ingestDocuments(List<Document> documents) {
        List<Document> chunks = split(documents);
        vectorStore.add(chunks);
        log.info("[AiRagService] 文档入库完成，分片数={}", chunks.size());
    }

    // ── 语义检索 ──────────────────────────────────────────────────────────────

    public List<Document> search(String query) {
        AiProperties.Rag cfg = aiProperties.getRag();
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(cfg.getTopK())
                        .similarityThreshold(cfg.getSimilarityThreshold())
                        .build()
        );
    }

    public String searchAsContext(String query) {
        return search(query).stream()
                .map(Document::getFormattedContent)
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    // ── RAG 问答 ──────────────────────────────────────────────────────────────

    public String ragChat(String question) {
        try {
            AiProperties.Rag cfg = aiProperties.getRag();
            return chatClient.prompt()
                    .user(question)
                    .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                            .searchRequest(SearchRequest.builder()
                                    .topK(cfg.getTopK())
                                    .similarityThreshold(cfg.getSimilarityThreshold())
                                    .build())
                            .build())
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("[AiRagService] RAG 问答失败: {}", e.getMessage(), e);
            throw new AiException("RAG 问答失败: " + e.getMessage(), e);
        }
    }

    public String ragChatWithSystem(String question, String systemPrompt) {
        try {
            AiProperties.Rag cfg = aiProperties.getRag();
            return chatClient.prompt()
                    .system(systemPrompt)
                    .user(question)
                    .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                            .searchRequest(SearchRequest.builder()
                                    .topK(cfg.getTopK())
                                    .similarityThreshold(cfg.getSimilarityThreshold())
                                    .build())
                            .build())
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("[AiRagService] RAG 问答失败: {}", e.getMessage(), e);
            throw new AiException("RAG 问答失败: " + e.getMessage(), e);
        }
    }

    // ── 私有方法 ───────────────────────────────────────────────────────────────

    private List<Document> split(List<Document> docs) {
        AiProperties.Rag cfg = aiProperties.getRag();
        return new TokenTextSplitter(
        ).apply(docs);
    }
}
