import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PositionBasedExtractor {

    /**
     * 根据标签和值的位置关系提取
     */
    public static Map<String, String> extractByPosition(
            List<String> texts,
            List<List<Integer>> boxes) {

        Map<String, String> result = new HashMap<>();

        // 定义字段标签
        String[] labels = {"注册号", "名称", "住所", "法定代表人", "注册资本",
                "公司类型", "成立日期", "营业期限", "经营范围"};

        for (int i = 0; i < texts.size(); i++) {
            String text = texts.get(i);
            List<Integer> box = boxes.get(i);

            // 查找是否为标签
            for (String label : labels) {
                if (text.contains(label) || isSimilar(text, label)) {
                    // 查找最近的值（右侧或下方）
                    String value = findNearestValue(i, texts, boxes, box);
                    if (value != null && !value.isEmpty()) {
                        result.put(label, value);
                    }
                    break;
                }
            }
        }

        return result;
    }

    /**
     * 查找最近的值（基于坐标）
     */
    private static String findNearestValue(int labelIndex,
                                           List<String> texts,
                                           List<List<Integer>> boxes,
                                           List<Integer> labelBox) {

        int labelX = labelBox.get(0);
        int labelY = labelBox.get(1);
        int labelRight = labelBox.get(2);
        int labelBottom = labelBox.get(3);

        String nearestValue = null;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < texts.size(); i++) {
            if (i == labelIndex) continue;

            List<Integer> box = boxes.get(i);
            int x = box.get(0);
            int y = box.get(1);

            // 计算距离（优先考虑右侧和下方）
            double distance;

            // 右侧（同一行）
            if (Math.abs(y - labelY) < 20 && x > labelRight) {
                distance = x - labelRight;
            }
            // 下方
            else if (y > labelBottom && Math.abs(x - labelX) < 100) {
                distance = (y - labelBottom) + Math.abs(x - labelX) * 0.5;
            }
            // 其他位置
            else {
                continue;
            }

            if (distance < minDistance) {
                minDistance = distance;
                nearestValue = texts.get(i);
            }
        }

        return nearestValue;
    }

    /**
     * 模糊匹配
     */
    private static boolean isSimilar(String text, String label) {
        return text.contains(label) || label.contains(text);
    }
}