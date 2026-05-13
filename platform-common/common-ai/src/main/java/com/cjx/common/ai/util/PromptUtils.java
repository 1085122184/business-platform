package com.cjx.common.ai.util;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.ClassPathResource;

import java.util.Map;

/**
 * Prompt 模板工具类
 * @author cuijixu
 */
public final class PromptUtils {

    private PromptUtils() {}

    /**
     * 变量替换渲染
     * <pre>
     * String p = PromptUtils.render("请将「{text}」翻译为{lang}", Map.of("text","你好","lang","英文"));
     * </pre>
     */
    public static String render(String template, Map<String, Object> vars) {
        return new PromptTemplate(template).render(vars);
    }

    /**
     * 从 classpath 加载 .st 模板文件并渲染
     * <pre>
     * // resources/prompts/translate.st: 请将「{text}」翻译为{lang}
     * String p = PromptUtils.renderFromFile("prompts/translate.st", Map.of(...));
     * </pre>
     */
    public static String renderFromFile(String classpathPath, Map<String, Object> vars) {
        return new PromptTemplate(new ClassPathResource(classpathPath)).render(vars);
    }

    /** 构建标准 RAG 提示词 */
    public static String buildRagPrompt(String question, String context) {
        return render("""
                请根据以下参考资料回答问题。若资料中无相关内容，请如实说明，不要编造。

                参考资料：
                {context}

                问题：{question}
                """, Map.of("context", context, "question", question));
    }
}
