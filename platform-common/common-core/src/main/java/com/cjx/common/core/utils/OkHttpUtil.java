package com.cjx.common.core.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp3 高性能HTTP请求工具类
 *
 * @author cuijixu
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OkHttpUtil {
    private final ObjectMapper objectMapper;
    private OkHttpClient okHttpClient;

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType FORM = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    /**
     * 初始化OkHttpClient（连接池配置）
     */
    @PostConstruct
    public void init() {
        ConnectionPool connectionPool = new ConnectionPool(200, 5, TimeUnit.MINUTES);

        okHttpClient = new OkHttpClient.Builder()
                .connectionPool(connectionPool)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(new LoggingInterceptor())
                .build();

        log.info("OkHttpClient initialized with connection pool");
    }

    /**
     * 销毁时关闭连接池
     */
    @PreDestroy
    public void destroy() {
        if (okHttpClient != null) {
            okHttpClient.dispatcher().executorService().shutdown();
            okHttpClient.connectionPool().evictAll();
            log.info("OkHttpClient destroyed");
        }
    }

    /**
     * GET请求
     *
     * @param url 请求URL
     * @return 响应字符串
     */
    public String get(String url) throws IOException {
        return get(url, null);
    }

    /**
     * GET请求（带请求头）
     *
     * @param url     请求URL
     * @param headers 请求头
     * @return 响应字符串
     */
    public String get(String url, Map<String, String> headers) throws IOException {
        Request.Builder builder = new Request.Builder().url(url).get();
        addHeaders(builder, headers);

        try (Response response = okHttpClient.newCall(builder.build()).execute()) {
            return getResponseBody(response);
        }
    }

    /**
     * POST请求（JSON格式）
     *
     * @param url  请求URL
     * @param json JSON字符串
     * @return 响应字符串
     */
    public String postJson(String url, String json) throws IOException {
        return postJson(url, json, null);
    }

    /**
     * POST请求（JSON格式，带请求头）
     *
     * @param url     请求URL
     * @param json    JSON字符串
     * @param headers 请求头
     * @return 响应字符串
     */
    public String postJson(String url, String json, Map<String, String> headers) throws IOException {
        RequestBody body = RequestBody.create(json, JSON);
        Request.Builder builder = new Request.Builder().url(url).post(body);
        addHeaders(builder, headers);

        try (Response response = okHttpClient.newCall(builder.build()).execute()) {
            return getResponseBody(response);
        }
    }

    /**
     * POST请求（对象自动转JSON）
     *
     * @param url    请求URL
     * @param object 请求对象
     * @return 响应字符串
     */
    public String postObject(String url, Object object) throws IOException {
        return postObject(url, object, null);
    }

    /**
     * POST请求（对象自动转JSON，带请求头）
     *
     * @param url     请求URL
     * @param object  请求对象
     * @param headers 请求头
     * @return 响应字符串
     */
    public String postObject(String url, Object object, Map<String, String> headers) throws IOException {
        String json = objectMapper.writeValueAsString(object);
        return postJson(url, json, headers);
    }

    /**
     * POST请求（表单格式）
     *
     * @param url    请求URL
     * @param params 表单参数
     * @return 响应字符串
     */
    public String postForm(String url, Map<String, String> params) throws IOException {
        return postForm(url, params, null);
    }

    /**
     * POST请求（表单格式，带请求头）
     *
     * @param url     请求URL
     * @param params  表单参数
     * @param headers 请求头
     * @return 响应字符串
     */
    public String postForm(String url, Map<String, String> params, Map<String, String> headers) throws IOException {
        FormBody.Builder formBuilder = new FormBody.Builder();
        if (params != null) {
            params.forEach(formBuilder::add);
        }

        Request.Builder builder = new Request.Builder().url(url).post(formBuilder.build());
        addHeaders(builder, headers);

        try (Response response = okHttpClient.newCall(builder.build()).execute()) {
            return getResponseBody(response);
        }
    }

    /**
     * PUT请求（JSON格式）
     *
     * @param url  请求URL
     * @param json JSON字符串
     * @return 响应字符串
     */
    public String putJson(String url, String json) throws IOException {
        return putJson(url, json, null);
    }

    /**
     * PUT请求（JSON格式，带请求头）
     *
     * @param url     请求URL
     * @param json    JSON字符串
     * @param headers 请求头
     * @return 响应字符串
     */
    public String putJson(String url, String json, Map<String, String> headers) throws IOException {
        RequestBody body = RequestBody.create(json, JSON);
        Request.Builder builder = new Request.Builder().url(url).put(body);
        addHeaders(builder, headers);

        try (Response response = okHttpClient.newCall(builder.build()).execute()) {
            return getResponseBody(response);
        }
    }

    /**
     * DELETE请求
     *
     * @param url 请求URL
     * @return 响应字符串
     */
    public String delete(String url) throws IOException {
        return delete(url, null);
    }

    /**
     * DELETE请求（带请求头）
     *
     * @param url     请求URL
     * @param headers 请求头
     * @return 响应字符串
     */
    public String delete(String url, Map<String, String> headers) throws IOException {
        Request.Builder builder = new Request.Builder().url(url).delete();
        addHeaders(builder, headers);

        try (Response response = okHttpClient.newCall(builder.build()).execute()) {
            return getResponseBody(response);
        }
    }

    /**
     * PATCH请求（JSON格式）
     *
     * @param url  请求URL
     * @param json JSON字符串
     * @return 响应字符串
     */
    public String patchJson(String url, String json) throws IOException {
        return patchJson(url, json, null);
    }

    /**
     * PATCH请求（JSON格式，带请求头）
     *
     * @param url     请求URL
     * @param json    JSON字符串
     * @param headers 请求头
     * @return 响应字符串
     */
    public String patchJson(String url, String json, Map<String, String> headers) throws IOException {
        RequestBody body = RequestBody.create(json, JSON);
        Request.Builder builder = new Request.Builder().url(url).patch(body);
        addHeaders(builder, headers);

        try (Response response = okHttpClient.newCall(builder.build()).execute()) {
            return getResponseBody(response);
        }
    }

    /**
     * 文件上传
     *
     * @param url      请求URL
     * @param file     文件
     * @param fileName 文件名
     * @param params   其他表单参数
     * @return 响应字符串
     */
    public String uploadFile(String url, File file, String fileName, Map<String, String> params) throws IOException {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        if (params != null) {
            params.forEach(builder::addFormDataPart);
        }

        RequestBody fileBody = RequestBody.create(file, MediaType.parse("application/octet-stream"));
        builder.addFormDataPart("file", fileName, fileBody);

        Request request = new Request.Builder()
                .url(url)
                .post(builder.build())
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            return getResponseBody(response);
        }
    }

    /**
     * 异步GET请求
     *
     * @param url      请求URL
     * @param callback 回调
     */
    public void getAsync(String url, Callback callback) {
        Request request = new Request.Builder().url(url).get().build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    /**
     * 异步POST请求
     *
     * @param url      请求URL
     * @param json     JSON字符串
     * @param callback 回调
     */
    public void postJsonAsync(String url, String json, Callback callback) {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder().url(url).post(body).build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    /**
     * 添加请求头
     *
     * @param builder Request.Builder
     * @param headers 请求头Map
     */
    private void addHeaders(Request.Builder builder, Map<String, String> headers) {
        if (headers != null && !headers.isEmpty()) {
            headers.forEach(builder::addHeader);
        }
    }

    /**
     * 获取响应体
     *
     * @param response Response对象
     * @return 响应字符串
     */
    private String getResponseBody(Response response) throws IOException {
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }

        ResponseBody body = response.body();
        if (body == null) {
            throw new IOException("Response body is null");
        }

        return body.string();
    }

    /**
     * 日志拦截器
     */
    private static class LoggingInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            long startTime = System.nanoTime();

            log.info("Sending request: {} {}", request.method(), request.url());

            Response response = chain.proceed(request);
            long endTime = System.nanoTime();

            log.info("Received response: {} {} in {}ms",
                    response.code(),
                    request.url(),
                    TimeUnit.NANOSECONDS.toMillis(endTime - startTime));

            return response;
        }
    }
}
