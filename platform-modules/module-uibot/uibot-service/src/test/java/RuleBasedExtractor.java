import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class RuleBasedExtractor {

    // 定义提取规则
    private static class ExtractionRule {
        String fieldName;
        Predicate<String> labelMatcher;  // 标签匹配规则
        Predicate<String> valueMatcher;  // 值匹配规则
        boolean needNextLine;            // 是否需要读取下一行

        public ExtractionRule(String fieldName,
                              Predicate<String> labelMatcher,
                              Predicate<String> valueMatcher,
                              boolean needNextLine) {
            this.fieldName = fieldName;
            this.labelMatcher = labelMatcher;
            this.valueMatcher = valueMatcher;
            this.needNextLine = needNextLine;
        }
    }

    private static List<ExtractionRule> rules = Arrays.asList(
            // 统一社会信用代码
            new ExtractionRule(
                    "统一社会信用代码",
                    text -> false,
                    text -> text.matches("^[0-9A-Z]{18}$"),
                    false
            ),

            // 注册号
            new ExtractionRule(
                    "注册号",
                    text -> text.contains("注册号"),
                    text -> text.matches("^\\d{12,15}$"),
                    true
            ),

            // 企业名称
            new ExtractionRule(
                    "企业名称",
                    text -> text.contains("名称") || text.equals("称"),
                    text -> (text.contains("公司") || text.contains("企业"))
                            && text.length() > 6,
                    true
            ),

            // 法定代表人
            new ExtractionRule(
                    "法定代表人",
                    text -> text.contains("法定代表人"),
                    text -> text.length() >= 2 && text.length() <= 4
                            && text.matches("[\\u4e00-\\u9fa5]+"),
                    true
            ),

            // 注册资本
            new ExtractionRule(
                    "注册资本",
                    text -> text.contains("注册资本"),
                    text -> text.matches(".*\\d+.*万.*元.*"),
                    true
            ),

            // 成立日期
            new ExtractionRule(
                    "成立日期",
                    text -> text.contains("成立日"),
                    text -> text.matches(".*\\d{4}年\\d{1,2}月\\d{1,2}日.*"),
                    true
            ),

            // 营业期限
            new ExtractionRule(
                    "营业期限",
                    text -> text.contains("营业期限"),
                    text -> text.contains("至") && text.contains("年"),
                    true
            )
    );

    /**
     * 基于规则提取
     */
    public static Map<String, String> extract(List<String> texts) {
        Map<String, String> result = new HashMap<>();

        for (int i = 0; i < texts.size(); i++) {
            String text = texts.get(i).trim();
            String nextText = i + 1 < texts.size() ? texts.get(i + 1).trim() : "";

            for (ExtractionRule rule : rules) {
                // 先检查当前文本是否匹配值规则
                if (rule.valueMatcher.test(text)) {
                    result.putIfAbsent(rule.fieldName, text);
                    continue;
                }

                // 检查是否为标签
                if (rule.labelMatcher.test(text)) {
                    if (rule.needNextLine && rule.valueMatcher.test(nextText)) {
                        result.put(rule.fieldName, nextText);
                    }
                }
            }
        }

        return result;
    }
}