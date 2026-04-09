package com.cjx.common.ai.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 内置工具函数示例（Spring AI 1.1.3 @Tool 注解）
 *
 * <p>业务模块可仿照此类添加自定义工具，在调用时传入 {@code .tools(yourTool)} 即可。
 */
@Slf4j
@Component
public class BuiltinTools {

    @Tool(description = "获取当前日期和时间，格式：yyyy-MM-dd HH:mm:ss")
    public String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Tool(description = "执行简单数学运算，支持 +、-、*、/")
    public double calculate(
            @ToolParam(description = "第一个数字") double a,
            @ToolParam(description = "运算符：+、-、*、/") String operator,
            @ToolParam(description = "第二个数字") double b) {
        return switch (operator) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> {
                if (b == 0) throw new ArithmeticException("除数不能为 0");
                yield a / b;
            }
            default -> throw new IllegalArgumentException("不支持的运算符: " + operator);
        };
    }

    @Tool(description = "统计文本的字符数（不含空格）")
    public int countCharacters(@ToolParam(description = "待统计文本") String text) {
        return text == null ? 0 : text.replaceAll("\\s", "").length();
    }
}
