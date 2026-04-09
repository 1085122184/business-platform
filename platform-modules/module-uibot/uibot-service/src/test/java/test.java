import cn.hutool.http.HttpUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class test {


    private static final String API_URL = "http://127.0.0.1:1224/api/ocr"; // 替换为实际API地址
    public static void main(String[] args) {
        String imagePath = "D:/桌面/ims.jpg"; // 图片路径
        String base64Image = encodeImageToBase64(imagePath);
        if (base64Image == null) {
            System.out.println("无法加载图片");
            return;
        }

        try {
            String response = sendPostRequest(API_URL, base64Image);
            System.out.println("OCR 结果：" + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
