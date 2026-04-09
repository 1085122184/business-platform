package com.cjx.common.dingtalk.utils;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cjx.common.core.manager.MapCacheManager;
import com.cjx.common.core.utils.OkHttpUtil;
import com.cjx.common.dingtalk.config.DingTalkCacheConfig;
import com.cjx.common.dingtalk.config.DingTalkConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 钉钉工具类
 *
 * @author claude
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DingTalkUtil {
    private final OkHttpUtil okHttpUtil;
    private final DingTalkConfig dingTalkConfig;
    private final MapCacheManager cacheManager;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    /**
     * 获取企业内部应用的access_token（带缓存）
     */
    public String getAccessToken() throws Exception {
        return cacheManager.getOrLoad(
                DingTalkCacheConfig.ACCESS_TOKEN_KEY,
                String.class,
                DingTalkCacheConfig.ACCESS_TOKEN_TTL,
                this::fetchAccessToken
        );
    }

    /**
     * 从钉钉服务器获取AccessToken
     */
    private String fetchAccessToken() throws IOException {
        String url = dingTalkConfig.getApiUrl() + "/gettoken?appkey=" + dingTalkConfig.getAppKey() + "&appsecret=" + dingTalkConfig.getAppSecret();
        String response = okHttpUtil.get(url);
        Map<String, Object> result = OBJECT_MAPPER.readValue(response, Map.class);
        if ((Integer) result.get("errcode") == 0) {
            String accessToken = (String) result.get("access_token");
            log.info("成功获取AccessToken，已缓存");
            return accessToken;
        }
        throw new RuntimeException("获取access_token失败: " + result.get("errmsg"));
    }

    /**
     * 强制刷新AccessToken
     */
    public String refreshAccessToken() throws IOException {
        cacheManager.remove(DingTalkCacheConfig.ACCESS_TOKEN_KEY);
        try {
            return getAccessToken();
        } catch (Exception e) {
            throw new IOException("刷新AccessToken失败", e);
        }
    }

    /**
     * 获取jsapi_ticket（带缓存）
     */
    public String getJsApiTicket() throws Exception {
        return cacheManager.getOrLoad(
                DingTalkCacheConfig.JSAPI_TICKET_KEY,
                String.class,
                DingTalkCacheConfig.JSAPI_TICKET_TTL,
                () -> fetchJsApiTicket(getAccessToken())
        );
    }

    /**
     * 从钉钉服务器获取JsApiTicket
     */
    private String fetchJsApiTicket(String accessToken) throws IOException {
        String url = dingTalkConfig.getApiUrl() + "/get_jsapi_ticket?access_token=" + accessToken;
        String response = okHttpUtil.get(url);
        Map<String, Object> result = OBJECT_MAPPER.readValue(response, Map.class);

        if ((Integer) result.get("errcode") == 0) {
            String ticket = (String) result.get("ticket");
            log.info("成功获取JsApiTicket，已缓存");
            return ticket;
        }
        throw new RuntimeException("获取jsapi_ticket失败: " + result.get("errmsg"));
    }


    /**
     * 生成JSAPI签名
     */
    public Map<String, String> generateJsApiSignature(String url)  throws Exception {

        String ticket = getJsApiTicket();
        // 生成签名
        String nonceStr = IdUtil.fastSimpleUUID();
        long timeStamp = System.currentTimeMillis();

        String plain = "jsapi_ticket=" + ticket +
                "&noncestr=" + nonceStr +
                "&timestamp=" + timeStamp +
                "&url=" + url;

        String signature = DigestUtil.sha1Hex(plain.getBytes(StandardCharsets.UTF_8));

        Map<String, String> map = new HashMap<>();
        map.put("agentId", String.valueOf(dingTalkConfig.getAgentId()));
        map.put("appKey", dingTalkConfig.getAppKey());
        map.put("timeStamp", String.valueOf(timeStamp));
        map.put("nonceStr", nonceStr);
        map.put("signature", signature);

        return map;
    }



    // ==================== 消息发送 ====================

    /**
     * 发送工作通知消息
     */
    public String sendWorkMessage(List<String> userIdList, Object msg) throws Exception {
        String accessToken = getAccessToken();
        String url = dingTalkConfig.getApiUrl() + "/topapi/message/corpconversation/asyncsend_v2?access_token=" + accessToken;

        Map<String, Object> params = new HashMap<>();
        params.put("agent_id", dingTalkConfig.getAgentId());
        params.put("userid_list", String.join(",", userIdList));
        params.put("msg", msg);
        return okHttpUtil.postObject(url, params);
    }

    /**
     * 发送文本消息
     */
    public String sendTextMessage(List<String> userIdList, String content) throws Exception {
        Map<String, Object> msg = new HashMap<>();
        msg.put("msgtype", "text");
        Map<String, String> text = new HashMap<>();
        text.put("content", content);
        msg.put("text", text);

        return sendWorkMessage(userIdList, msg);
    }

    /**
     * 发送Markdown消息
     */
    public String sendMarkdownMessage(List<String> userIdList, String title, String text) throws Exception {
        Map<String, Object> msg = new HashMap<>();
        msg.put("msgtype", "markdown");
        Map<String, String> markdown = new HashMap<>();
        markdown.put("title", title);
        markdown.put("text", text);
        msg.put("markdown", markdown);

        return sendWorkMessage(userIdList, msg);
    }

    /**
     * 发送链接消息
     */
    public String sendLinkMessage(List<String> userIdList, String title, String text,
                                  String messageUrl, String picUrl) throws Exception {
        Map<String, Object> msg = new HashMap<>();
        msg.put("msgtype", "link");
        Map<String, String> link = new HashMap<>();
        link.put("title", title);
        link.put("text", text);
        link.put("messageUrl", messageUrl);
        link.put("picUrl", picUrl);
        msg.put("link", link);

        return sendWorkMessage(userIdList, msg);
    }


    // ==================== 机器人消息 ====================

    /**
     * 自定义机器人发送消息
     */
    public String sendRobotMessage(String webhook, Map<String, Object> message) throws IOException {
        return okHttpUtil.postObject(webhook, message);
    }

    /**
     * 机器人发送文本消息
     */
    public String sendRobotTextMessage(String webhook, String content,
                                       List<String> atMobiles, boolean isAtAll) throws IOException {
        Map<String, Object> message = new HashMap<>();
        message.put("msgtype", "text");

        Map<String, Object> text = new HashMap<>();
        text.put("content", content);
        message.put("text", text);

        Map<String, Object> at = new HashMap<>();
        at.put("atMobiles", atMobiles != null ? atMobiles : Collections.emptyList());
        at.put("isAtAll", isAtAll);
        message.put("at", at);

        return sendRobotMessage(webhook, message);
    }


    // ==================== 用户管理（带缓存） ====================

    /**
     * 根据userid获取用户详情（带缓存）
     */
    public String getUserInfo(String userId) throws Exception {
        String cacheKey = DingTalkCacheConfig.buildUserInfoKey(userId);
        return cacheManager.getOrLoad(
                cacheKey,
                String.class,
                DingTalkCacheConfig.USER_INFO_TTL,
                () -> fetchUserInfo(getAccessToken(), userId)
        );
    }

    /**
     * 从钉钉服务器获取用户信息
     */
    private String fetchUserInfo(String accessToken, String userId) throws IOException {
        String url = dingTalkConfig.getApiUrl() + "/topapi/v2/user/get?access_token=" + accessToken;
        Map<String, Object> params = new HashMap<>();
        params.put("userid", userId);
        String result = okHttpUtil.postObject(url, params);
        log.info("成功获取用户信息: userId={}", userId);
        return result;
    }

    /**
     * 清除用户信息缓存
     */
    public void clearUserInfoCache(String userId) {
        cacheManager.remove(DingTalkCacheConfig.buildUserInfoKey(userId));
    }

    /**
     * 根据手机号获取userid
     */
    public String getUserIdByMobile(String mobile) throws Exception {
        String accessToken = getAccessToken();
        String url = dingTalkConfig.getApiUrl() + "/topapi/v2/user/getbymobile?access_token=" + accessToken;
        Map<String, Object> params = new HashMap<>();
        params.put("mobile", mobile);
        return okHttpUtil.postObject(url, params);
    }

    // ==================== 部门管理（带缓存） ====================

    /**
     * 获取部门详情（带缓存）
     */
    public String getDepartmentInfo(Long deptId) throws Exception {
        String cacheKey = DingTalkCacheConfig.buildDeptInfoKey(deptId);
        return cacheManager.getOrLoad(
                cacheKey,
                String.class,
                DingTalkCacheConfig.DEPT_INFO_TTL,
                () -> fetchDepartmentInfo(getAccessToken(), deptId)
        );
    }

    /**
     * 从钉钉服务器获取部门信息
     */
    private String fetchDepartmentInfo(String accessToken, Long deptId) throws IOException {
        String url = dingTalkConfig.getApiUrl() + "/topapi/v2/department/get?access_token=" + accessToken;
        Map<String, Object> params = new HashMap<>();
        params.put("dept_id", deptId);
        String result = okHttpUtil.postObject(url, params);
        log.info("成功获取部门信息: deptId={}", deptId);
        return result;
    }

    /**
     * 清除部门信息缓存
     */
    public void clearDepartmentInfoCache(Long deptId) {
        cacheManager.remove(DingTalkCacheConfig.buildDeptInfoKey(deptId));
    }

    /**
     * 获取部门列表
     */
    public String getDepartmentList(Long deptId) throws Exception {
        String accessToken = getAccessToken();
        String url = dingTalkConfig.getApiUrl() + "/topapi/v2/department/listsub?access_token=" + accessToken;
        Map<String, Object> params = new HashMap<>();
        params.put("dept_id", deptId);
        return okHttpUtil.postObject(url, params);
    }

    // ==================== 缓存管理 ====================

    /**
     * 清除所有钉钉相关缓存
     */
    public void clearAllCache() {
        cacheManager.clear();
        log.info("已清除所有钉钉缓存");
    }

    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSize", cacheManager.size());
        stats.put("accessTokenTtl", cacheManager.getRemainTtl(DingTalkCacheConfig.ACCESS_TOKEN_KEY));
        stats.put("jsApiTicketTtl", cacheManager.getRemainTtl(DingTalkCacheConfig.JSAPI_TICKET_KEY));
        return stats;
    }




    //==========================================================
    /**
     * 根据authCode获取用户信息
     */
    public JSONObject getUserInfoByAuthCode(String authCode) throws Exception {
        String accessToken = getAccessToken();
        String url = dingTalkConfig.getApiUrl() + "/topapi/v2/user/getuserinfo";

        Map<String, Object> params = new HashMap<>();
        params.put("access_token", accessToken);
        params.put("code", authCode);

        String result = HttpUtil.post(url, JSONUtil.toJsonStr(params));
        JSONObject jsonObject = JSONUtil.parseObj(result);

        if (jsonObject.getInt("errcode") == 0) {
            return jsonObject.getJSONObject("result");
        }

        log.error("获取钉钉用户信息失败: {}", result);
        throw new RuntimeException("获取钉钉用户信息失败");
    }


    /**
     * 根据userId获取用户详细信息
     */
    public JSONObject getUserDetail(String userId)  throws Exception {
        String accessToken = getAccessToken();
        String url = dingTalkConfig.getApiUrl() + "/topapi/v2/user/get";

        Map<String, Object> params = new HashMap<>();
        params.put("access_token", accessToken);
        params.put("userid", userId);

        String result = HttpUtil.post(url, JSONUtil.toJsonStr(params));
        JSONObject jsonObject = JSONUtil.parseObj(result);

        if (jsonObject.getInt("errcode") == 0) {
            return jsonObject.getJSONObject("result");
        }

        log.error("获取用户详细信息失败: {}", result);
        throw new RuntimeException("获取用户详细信息失败");
    }

}
