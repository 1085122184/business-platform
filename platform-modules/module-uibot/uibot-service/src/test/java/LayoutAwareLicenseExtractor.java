import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 营业执照信息提取器（坐标感知版）
 * 特点：解决关键词拆分、值位置不定、多行字段等痛点
 */
public class LayoutAwareLicenseExtractor {

    // ==================== 1. 业务数据模型 ====================
    public static class BusinessLicense {
        private String creditCode;          // 统一社会信用代码
        private String registrationNumber;  // 注册号
        private String companyName;         // 企业名称
        private String legalRepresentative; // 法定代表人
        private String registeredCapital;   // 注册资本
        private String establishDate;       // 成立日期
        private String businessTerm;        // 营业期限
        private String address;             // 住所
        private String businessScope;       // 经营范围
        private String companyType;         // 公司类型

        // Getters & Setters (简化版，实际项目建议用Lombok)
        public String getCreditCode() { return creditCode; }
        public void setCreditCode(String creditCode) { this.creditCode = creditCode; }
        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        public String getRegistrationNumber() { return registrationNumber; }
        public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
        public String getLegalRepresentative() { return legalRepresentative; }
        public void setLegalRepresentative(String legalRepresentative) { this.legalRepresentative = legalRepresentative; }
        public String getRegisteredCapital() { return registeredCapital; }
        public void setRegisteredCapital(String registeredCapital) { this.registeredCapital = registeredCapital; }
        public String getEstablishDate() { return establishDate; }
        public void setEstablishDate(String establishDate) { this.establishDate = establishDate; }
        public String getBusinessTerm() { return businessTerm; }
        public void setBusinessTerm(String businessTerm) { this.businessTerm = businessTerm; }

        public String getAddress() { return businessTerm; }
        public void setAddress(String address) { this.address = address; }

        public String getBusinessScope() { return businessScope; }
        public void setBusinessScope(String businessScope) { this.businessScope = businessScope; }

        public String getCompanyType() { return companyType; }
        public void setCompanyType(String companyType) { this.companyType = companyType; }

        @Override
        public String toString() {
            return "BusinessLicense{\n" +
                    "  企业名称='" + companyName + "'\n" +
                    "  统一代码='" + creditCode + "'\n" +
                    "  注册号='" + registrationNumber + "'\n" +
                    "  法定代表人='" + legalRepresentative + "'\n" +
                    "  注册资本='" + registeredCapital + "'\n" +
                    "  成立日期='" + establishDate + "'\n" +
                    "  营业期限='" + businessTerm + "'\n" +
                    "  住所='" + address + "'\n" +
                    "  经营范围='" + businessScope + "'\n" +
                    "  公司类型='" + companyType + "'\n" +
                    '}';
        }
    }

    // ==================== 2. 文本块内部类 ====================
    private static class TextBlock {
        String text;
        int x1, y1, x2, y2; // 左上(x1,y1) 右下(x2,y2)
        double score;

        TextBlock(String text, int x1, int y1, int x2, int y2, double score) {
            this.text = text.trim();
            this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
            this.score = score;
        }

        int getCenterY() { return (y1 + y2) / 2; }
        int getWidth() { return x2 - x1; }
    }

    // ==================== 3. 核心提取方法 ====================
    /**
     * 从OCR结果JSON中提取营业执照信息
     * @param ocrJson OCR返回的完整JSON字符串（含prunedResult）
     * @return 提取的营业执照信息
     */
    public static BusinessLicense extract(String ocrJson) {
        try {
            JSONObject root = JSONObject.parseObject(ocrJson);
            JSONObject prunedResult = root;         

            // 验证必要字段
            if (prunedResult == null ||
                    prunedResult.getJSONArray("rec_texts") == null ||
                    prunedResult.getJSONArray("rec_boxes") == null) {
                throw new IllegalArgumentException("JSON缺少必要字段: prunedResult.rec_texts 或 rec_boxes");
            }

            return extractFromPrunedResult(prunedResult);
        } catch (Exception e) {
            System.err.println("【提取失败】" + e.getMessage());
            e.printStackTrace();
            return new BusinessLicense(); // 返回空对象避免NPE
        }
    }

    /**
     * 从prunedResult对象提取（便于单元测试）
     */
    public static BusinessLicense extractFromPrunedResult(JSONObject prunedResult) {
        BusinessLicense license = new BusinessLicense();

        // 1. 构建带坐标的文本块（过滤低置信度+空文本）
        List<TextBlock> blocks = buildTextBlocks(prunedResult);
        if (blocks.isEmpty()) return license;

        // 2. 按Y坐标分组重建行（容忍度15像素）
        List<List<TextBlock>> lines = groupByLine(blocks, 15);

        // 3. 合并被拆分的关键词（关键！解决"成立日"+"期"问题）
        mergeSplitKeywords(lines);

        // 4. 按空间关系精准提取字段
        extractSingleField(lines, "注册号", license::setRegistrationNumber);
        extractSingleField(lines, "统一社会信用代码|信用代码|社会信用代码", license::setCreditCode);
        extractSingleField(lines, "法定代表人", license::setLegalRepresentative);
        extractSingleField(lines, "注册资本", license::setRegisteredCapital);
        extractSingleField(lines, "成立日期", license::setEstablishDate);
        extractSingleField(lines, "营业期限", license::setBusinessTerm);
        extractSingleField(lines, "住所", license::setAddress);
        extractSingleField(lines, "公司类型", license::setCompanyType);
        extractMultiLineField(lines, "经营范围", license::setBusinessScope);

        // 5. 兜底：企业名称（含"有限公司"的文本）
        blocks.stream()
                .filter(b -> b.text.matches(".*[有责集]团.*公司 $ |.*有限公司 $ "))
                .max(Comparator.comparingInt(TextBlock::getWidth)) // 取最宽的（通常是标题）
                .ifPresent(b -> license.setCompanyName(b.text));

        // 6. 智能兜底：当信用代码缺失时，用注册号替代（旧版执照常见）
        if (StringUtils.isEmpty(license.getCreditCode()) &&
                StringUtils.isNotEmpty(license.getRegistrationNumber())) {
            license.setCreditCode(license.getRegistrationNumber());
        }

        return license;
    }

    // ==================== 4. 核心工具方法 ====================
    private static List<TextBlock> buildTextBlocks(JSONObject prunedResult) {
        JSONArray texts = prunedResult.getJSONArray("rec_texts");
        JSONArray boxes = prunedResult.getJSONArray("rec_boxes");
        JSONArray scores = prunedResult.getJSONArray("rec_scores");
        List<TextBlock> blocks = new ArrayList<>();

        for (int i = 0; i < texts.size(); i++) {
            String text = texts.getString(i);
            if (StringUtils.isEmpty(text) || " ".equals(text)) continue;

            // 置信度过滤（低于0.6视为噪声）
            double score = (scores != null && i < scores.size()) ? scores.getDoubleValue(i) : 1.0;
            if (score < 0.6) continue;

            // 解析坐标（rec_boxes格式: [x1, y1, x2, y2]）
            JSONArray box = boxes.getJSONArray(i);
            if (box.size() < 4) continue;

            blocks.add(new TextBlock(
                    text,
                    box.getInteger(0), // x1
                    box.getInteger(1), // y1
                    box.getInteger(2), // x2
                    box.getInteger(3), // y2
                    score
            ));
        }
        return blocks;
    }

    private static List<List<TextBlock>> groupByLine(List<TextBlock> blocks, int tolerance) {
        blocks.sort(Comparator.comparingInt(TextBlock::getCenterY));
        List<List<TextBlock>> lines = new ArrayList<>();
        List<TextBlock> currentLine = new ArrayList<>();

        for (int i = 0; i < blocks.size(); i++) {
            if (i == 0) {
                currentLine.add(blocks.get(i));
                continue;
            }

            TextBlock prev = blocks.get(i - 1);
            TextBlock curr = blocks.get(i);
            if (Math.abs(curr.getCenterY() - prev.getCenterY()) <= tolerance) {
                currentLine.add(curr);
            } else {
                lines.add(new ArrayList<>(currentLine));
                currentLine.clear();
                currentLine.add(curr);
            }
        }
        if (!currentLine.isEmpty()) lines.add(currentLine);

        // 每行内按X坐标排序
        lines.forEach(line -> line.sort(Comparator.comparingInt(b -> b.x1)));
        return lines;
    }

    private static void mergeSplitKeywords(List<List<TextBlock>> lines) {
        // 定义常见拆分模式（关键词 -> 碎片数组）
        Map<String, String[]> splitPatterns = new HashMap<>();
        splitPatterns.put("成立日期", new String[]{"成立日", "期"});
        splitPatterns.put("营业期限", new String[]{"营业", "期", "限"});
        splitPatterns.put("经营范围", new String[]{"经营范", "围"});
        splitPatterns.put("法定代表人", new String[]{"法定代表", "人"});

        for (List<TextBlock> line : lines) {
            for (Map.Entry<String, String[]> entry : splitPatterns.entrySet()) {
                String target = entry.getKey();
                String[] fragments = entry.getValue();

                // 检查当前行是否包含所有碎片
                boolean hasAllFragments = Arrays.stream(fragments).allMatch(fragment ->
                        line.stream().anyMatch(b -> b.text.contains(fragment))
                );

                if (hasAllFragments) {
                    // 合并为完整关键词（取第一个碎片的坐标）
                    TextBlock first = line.stream()
                            .filter(b -> b.text.contains(fragments[0]))
                            .findFirst().orElse(line.get(0));

                    TextBlock merged = new TextBlock(target,
                            first.x1, first.y1,
                            line.get(line.size()-1).x2, first.y2,
                            1.0);

                    // 替换整行
                    line.clear();
                    line.add(merged);
                    break; // 处理完一个关键词即跳出
                }
            }
        }
    }

    private static void extractSingleField(List<List<TextBlock>> lines,
                                           String keywordRegex,
                                           Consumer<String> setter) {
        Pattern pattern = Pattern.compile(keywordRegex);

        for (int i = 0; i < lines.size(); i++) {
            Optional<TextBlock> labelOpt = lines.get(i).stream()
                    .filter(b -> pattern.matcher(b.text).find())
                    .findFirst();

            if (labelOpt.isPresent()) {
                TextBlock label = labelOpt.get();
                String value = null;

                // 策略1：同一行右侧（最常见）
                value = findRightValue(lines.get(i), label);
                if (StringUtils.isNotEmpty(value)) {
                    setter.accept(value);
                    return;
                }

                // 策略2：下一行（标签单独占一行）
                if (i + 1 < lines.size()) {
                    value = String.join("", lines.get(i+1).stream()
                            .map(b -> b.text).collect(Collectors.joining(" ")));
                    // 避免取到下一个字段标签
                    if (!value.matches(".*(注册号|名称|法定代表人|注册资本|成立日期|营业期限|公司类型|住所).*")) {
                        setter.accept(value);
                        return;
                    }
                }

                // 策略3：上一行（如注册号在"注册号"标签前）
                if (i > 0) {
                    value = String.join("", lines.get(i-1).stream()
                            .map(b -> b.text).collect(Collectors.joining(" ")));
                    setter.accept(value);
                    return;
                }
            }
        }
    }

    private static String findRightValue(List<TextBlock> line, TextBlock label) {
        return line.stream()
                .filter(b -> b.x1 > label.x2 && (b.x1 - label.x2) < 400) // 右侧400像素内
                .sorted(Comparator.comparingInt(b -> b.x1))
                .map(b -> b.text)
                .collect(Collectors.joining(" "));
    }

    private static void extractMultiLineField(List<List<TextBlock>> lines,
                                              String keyword,
                                              Consumer<String> setter) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).stream().anyMatch(b -> b.text.contains(keyword))) {
                StringBuilder sb = new StringBuilder();
                // 从下一行开始收集，直到遇到新字段标签
                for (int j = i + 1; j < lines.size(); j++) {
                    String lineText = String.join(" ", lines.get(j).stream()
                            .map(b -> b.text).collect(Collectors.joining(" ")));

                    // 遇到新字段标签则终止（避免跨字段）
                    if (lineText.matches(".*(年检|营业期限|成立日期|注册资本|法定代表人|注册号|公司类型|住所).*")) {
                        break;
                    }
                    sb.append(lineText).append(" ");
                }
                if (sb.length() > 0) {
                    setter.accept(sb.toString().trim());
                }
                return;
            }
        }
    }
}