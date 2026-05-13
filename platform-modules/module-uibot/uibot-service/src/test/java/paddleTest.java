import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

public class paddleTest {

    public static void main(String[] args) throws IOException {
        String API_URL = "http://192.168.200.18:8900/ocr";
        String imagePath = "E:/SF/ims.jpg";

        File file = new File(imagePath);
        byte[] fileContent = java.nio.file.Files.readAllBytes(file.toPath());
        String base64Image = Base64.getEncoder().encodeToString(fileContent);

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("file", base64Image);
        payload.put("fileType", 1);

        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, payload.toString());

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode result = root.get("result");

                JsonNode ocrResults = result.get("ocrResults");
                for (int i = 0; i < ocrResults.size(); i++) {
                    JsonNode item = ocrResults.get(i);

                    JsonNode prunedResult = item.get("prunedResult");
                    System.out.println("Pruned Result [" + i + "]: " + prunedResult.toString());

                    String ocrImageBase64 = item.get("ocrImage").asText();
                    byte[] ocrImageBytes = Base64.getDecoder().decode(ocrImageBase64);
                    String ocrImgPath = "ocr_result_" + i + ".jpg";
                    try (FileOutputStream fos = new FileOutputStream(ocrImgPath)) {
                        fos.write(ocrImageBytes);
                        System.out.println("Saved OCR image to: " + ocrImgPath);
                    }
                }
            } else {
                System.err.println("Request failed with HTTP code: " + response.code());
            }
        }
    }
}
