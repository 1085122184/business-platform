import com.cjx.uibot.service.entity.PaddleOCRResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmartLicenseExtractor {
    /**
     * 智能提取营业执照信息
     */
    public static Map<String, String> extractLicenseInfo(List<String> texts) {
        Map<String, String> result = new HashMap<>();

        for (int i = 0; i < texts.size(); i++) {
            String text = texts.get(i).trim();
            String nextText = i + 1 < texts.size() ? texts.get(i + 1).trim() : "";

            // 1. 企业名称（通常包含"公司"、"企业"等，且较长）
            if (text.contains("公司") || text.contains("企业")) {
                if (text.length() > 6 && !text.contains("类型") && !text.contains("经营")) {
                    result.putIfAbsent("企业名称", text);
                }
            }

            // 2. 统一社会信用代码（18位数字字母组合）
            if (text.matches("^[0-9A-Z]{18}$")) {
                result.put("统一社会信用代码", text);
            }

            // 3. 注册号（纯数字，12-15位）
            if (text.matches("^\\d{12,15}$")) {
                result.put("注册号", text);
            }

            // 4. 法定代表人
            if (text.contains("法定代表人") || text.equals("法定代表人姓名")) {
                if (!nextText.isEmpty() && nextText.length() <= 4 && isChinese(nextText)) {
                    result.put("法定代表人", nextText);
                }
            } else if (i > 0 && texts.get(i - 1).contains("法定代表人")) {
                if (text.length() <= 4 && isChinese(text)) {
                    result.put("法定代表人", text);
                }
            }

            // 5. 注册资本
            if (text.contains("注册资本")) {
                if (nextText.contains("万") || nextText.contains("元")) {
                    result.put("注册资本", nextText);
                }
            } else if (text.matches(".*\\d+万.*元.*")) {
                if (i > 0 && texts.get(i - 1).contains("注册资本")) {
                    result.put("注册资本", text);
                }
            }

            // 6. 成立日期
            if (text.contains("成立日") || text.equals("成立日期")) {
                String date = extractDate(nextText);
                if (date != null) {
                    result.put("成立日期", date);
                }
            } else {
                String date = extractDate(text);
                if (date != null && i > 0 && texts.get(i - 1).contains("成立")) {
                    result.put("成立日期", date);
                }
            }

            // 7. 营业期限
            if (text.contains("营业期限") && !text.contains("年")) {
                // 下一行可能是日期范围
                if (nextText.contains("至") || nextText.contains("年")) {
                    result.put("营业期限", nextText);
                }
            } else if (text.matches(".*\\d{4}年.*至.*\\d{4}年.*")) {
                result.put("营业期限", text);
            }

            // 8. 住所/地址
            if (text.contains("住所") || text.equals("住") || text.equals("所")) {
                if (nextText.length() > 8 && (nextText.contains("市") || nextText.contains("区") || nextText.contains("路"))) {
                    result.put("住所", nextText);
                }
            } else if (text.contains("市") && text.contains("路") && text.length() > 10) {
                if (i > 0 && (texts.get(i - 1).contains("住") || texts.get(i - 1).contains("所"))) {
                    result.put("住所", text);
                }
            }

            // 9. 公司类型
            if (text.contains("有限") && (text.contains("公司") || text.contains("责任"))) {
                if (i > 0 && texts.get(i - 1).contains("类型")) {
                    result.put("公司类型", text);
                }
            }

            // 10. 经营范围（通常是连续的多行文本）
            if (text.contains("经营范围")) {
                StringBuilder scope = new StringBuilder();
                for (int j = i + 1; j < texts.size() && j < i + 10; j++) {
                    String scopeText = texts.get(j);
                    // 如果遇到明显的其他字段，停止
                    if (scopeText.contains("成立日") || scopeText.contains("营业期限")
                            || scopeText.contains("年检") || scopeText.length() < 5) {
                        break;
                    }
                    scope.append(scopeText);
                }
                if (scope.length() > 0) {
                    result.put("经营范围", scope.toString());
                }
            }
        }

        return result;
    }

    /**
     * 提取日期（支持多种格式）
     */
    private static String extractDate(String text) {
        // 匹配：2009年12月11日
        Pattern p1 = Pattern.compile("(\\d{4})年(\\d{1,2})月(\\d{1,2})日");
        Matcher m1 = p1.matcher(text);
        if (m1.find()) {
            return m1.group(0);
        }

        // 匹配：2009-12-11
        Pattern p2 = Pattern.compile("(\\d{4})-(\\d{1,2})-(\\d{1,2})");
        Matcher m2 = p2.matcher(text);
        if (m2.find()) {
            return m2.group(0);
        }

        return null;
    }

    /**
     * 判断是否为中文
     */
    private static boolean isChinese(String text) {
        return text.matches("[\\u4e00-\\u9fa5]+");
    }


    @SneakyThrows
    public static void main(String[] args) {



        String jsonString = "{\"model_settings\":{\"use_doc_preprocessor\":true,\"use_textline_orientation\":true},\"dt_polys\":[[[223,141],[823,151],[822,249],[222,240]],[[812,210],[993,216],[993,239],[812,234]],[[770,240],[921,243],[921,263],[770,260]],[[239,274],[479,279],[479,302],[239,298]],[[675,268],[739,268],[739,296],[675,296]],[[191,307],[218,307],[218,336],[191,336]],[[238,316],[488,318],[488,341],[238,339]],[[57,345],[85,345],[85,374],[57,374]],[[191,345],[218,345],[218,376],[191,376]],[[235,358],[282,358],[282,385],[235,385]],[[775,355],[915,358],[915,382],[775,379]],[[57,386],[216,386],[216,413],[57,413]],[[236,394],[432,397],[432,420],[236,418]],[[575,386],[736,386],[736,413],[575,413]],[[774,392],[915,396],[915,419],[774,416]],[[53,420],[221,422],[221,457],[53,454]],[[573,421],[736,423],[736,454],[573,451]],[[234,433],[405,435],[405,458],[234,455]],[[54,461],[220,461],[220,495],[54,495]],[[235,453],[547,457],[547,479],[235,476]],[[234,472],[546,476],[546,499],[234,495]],[[236,493],[544,496],[544,517],[236,513]],[[236,512],[485,515],[485,535],[236,533]],[[607,536],[868,539],[868,562],[607,559]],[[606,559],[787,565],[786,592],[605,587]],[[233,597],[373,599],[373,623],[233,621]],[[56,627],[191,630],[191,657],[56,655]],[[193,630],[221,630],[221,655],[193,655]],[[231,631],[541,635],[541,660],[231,657]],[[704,646],[729,646],[729,670],[704,670]],[[791,646],[815,646],[815,670],[791,670]],[[828,644],[852,644],[852,663],[828,663]],[[882,648],[901,648],[901,668],[882,668]],[[59,662],[222,662],[222,690],[59,690]]],\"text_det_params\":{\"limit_side_len\":64,\"limit_type\":\"min\",\"thresh\":0.3,\"max_side_limit\":4000,\"box_thresh\":0.6,\"unclip_ratio\":1.5},\"text_type\":\"general\",\"textline_orientation_angles\":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],\"text_rec_score_thresh\":0.0,\"return_word_box\":false,\"rec_texts\":[\"企业法人营业执照\",\"000000201111220005S\",\"320000000084336\",\"江苏升阳创业投资有限公司\",\"注册号\",\"称\",\"南京市中山东路288号4503室\",\"住\",\"所\",\"杨升\",\"2000万元人民币\",\"法定代表人姓名\",\"有限公司（自然人控股）\",\"注册资本\",\"2000万元人民币\",\"公司类型\",\"实收资本\",\"许可经营项目：无。\",\"经营范围\",\"一般经营项目：创业投资，投资与资\",\"产管理，财务咨询，金属、电子、花\",\"工产品的销售，仓储，自营和代理各\",\"类商品及技术的进出口业务。\",\"每年三月一日至六月三十日年检！\",\"已参加2010年度年检\",\"2009年12月11日\",\"成立日\",\"期\",\"2009年12月11日至2019年12月1日\",\"年\",\"月\",\"司\",\"日\",\"营业期限\"],\"rec_scores\":[0.9989545345306396,0.8923020362854004,0.999224066734314,0.9993079304695129,0.9997429847717285,0.9999521970748901,0.9964127540588379,0.9994874000549316,0.9999885559082031,0.998737633228302,0.9964655637741089,0.9996629357337952,0.998457670211792,0.9996991157531738,0.9982466101646423,0.9998250007629395,0.9997740983963013,0.9871548414230347,0.9998322129249573,0.9872995018959045,0.9634774923324585,0.9789396524429321,0.9962353110313416,0.984765887260437,0.9975820183753967,0.9992956519126892,0.9999136924743652,0.9997754693031311,0.9989902377128601,0.9998493194580078,0.9999126195907593,0.10177598893642426,0.9949357509613037,0.999787449836731],\"rec_polys\":[[[223,141],[823,151],[822,249],[222,240]],[[812,210],[993,216],[993,239],[812,234]],[[770,240],[921,243],[921,263],[770,260]],[[239,274],[479,279],[479,302],[239,298]],[[675,268],[739,268],[739,296],[675,296]],[[191,307],[218,307],[218,336],[191,336]],[[238,316],[488,318],[488,341],[238,339]],[[57,345],[85,345],[85,374],[57,374]],[[191,345],[218,345],[218,376],[191,376]],[[235,358],[282,358],[282,385],[235,385]],[[775,355],[915,358],[915,382],[775,379]],[[57,386],[216,386],[216,413],[57,413]],[[236,394],[432,397],[432,420],[236,418]],[[575,386],[736,386],[736,413],[575,413]],[[774,392],[915,396],[915,419],[774,416]],[[53,420],[221,422],[221,457],[53,454]],[[573,421],[736,423],[736,454],[573,451]],[[234,433],[405,435],[405,458],[234,455]],[[54,461],[220,461],[220,495],[54,495]],[[235,453],[547,457],[547,479],[235,476]],[[234,472],[546,476],[546,499],[234,495]],[[236,493],[544,496],[544,517],[236,513]],[[236,512],[485,515],[485,535],[236,533]],[[607,536],[868,539],[868,562],[607,559]],[[606,559],[787,565],[786,592],[605,587]],[[233,597],[373,599],[373,623],[233,621]],[[56,627],[191,630],[191,657],[56,655]],[[193,630],[221,630],[221,655],[193,655]],[[231,631],[541,635],[541,660],[231,657]],[[704,646],[729,646],[729,670],[704,670]],[[791,646],[815,646],[815,670],[791,670]],[[828,644],[852,644],[852,663],[828,663]],[[882,648],[901,648],[901,668],[882,668]],[[59,662],[222,662],[222,690],[59,690]]],\"rec_boxes\":[[222,141,823,249],[812,210,993,239],[770,240,921,263],[239,274,479,302],[675,268,739,296],[191,307,218,336],[238,316,488,341],[57,345,85,374],[191,345,218,376],[235,358,282,385],[775,355,915,382],[57,386,216,413],[236,394,432,420],[575,386,736,413],[774,392,915,419],[53,420,221,457],[573,421,736,454],[234,433,405,458],[54,461,220,495],[235,453,547,479],[234,472,546,499],[236,493,544,517],[236,512,485,535],[607,536,868,562],[605,559,787,592],[233,597,373,623],[56,627,191,657],[193,630,221,655],[231,631,541,660],[704,646,729,670],[791,646,815,670],[828,644,852,663],[882,648,901,668],[59,662,222,690]]}";        // 1. 解析JSON
        ObjectMapper mapper = new ObjectMapper();
        PaddleOCRResult ocrResult = mapper.readValue(jsonString, PaddleOCRResult.class);

        // 2. 提取数据（三种方案任选）

        // 方案一：关键字匹配
        Map<String, String> info1 = SmartLicenseExtractor.extractLicenseInfo(
                ocrResult.getRecTexts()
        );

        // 方案二：位置关系
        Map<String, String> info2 = PositionBasedExtractor.extractByPosition(
                ocrResult.getRecTexts(),
                ocrResult.getRecBoxes()
        );
//
        // 方案三：规则引擎
        Map<String, String> info3 = RuleBasedExtractor.extract(
                ocrResult.getRecTexts()
        );

        // 3. 合并结果（优先级：方案一 > 方案二 > 方案三）
        Map<String, String> finalResult = new HashMap<>();
        finalResult.putAll(info3);
//        finalResult.putAll(info2);
//        finalResult.putAll(info1);

        // 4. 输出
        finalResult.forEach((k, v) ->
                System.out.println(k + ": " + v)
        );
    }
    /**
     * 将图片转换为Base64编码字符串
     */
    private static String encodeImageToBase64(String imagePath) {
        File file = new File(imagePath);
        try (FileInputStream imageInFile = new FileInputStream(file)) {
            byte[] imageData = imageInFile.readAllBytes(); // For Java 11+
            return Base64.getEncoder().encodeToString(imageData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 发送HTTP POST请求到指定URL，并附带Base64图片数据
     */
    private static String sendPostRequest(String apiUrl, String base64ImageData) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
//[[[214,437],[508,701]]]
        String jsonInputString = "{\"base64\": \"" + base64ImageData + "\",\"options\": { \"tbpu.ignoreArea\": [[[214,437],[508,701]]]},\"data.format\": \"dict\"}"; // 根据实际情况调整JSON格式
//        "options": { "tbpu.ignoreArea": [[[214,437],[508,701]]}
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }



}
