package com.cjx.uibot.service.controller;

import cn.hutool.json.JSONObject;
import com.cjx.common.core.result.Result;
import com.cjx.common.dingtalk.utils.DingTalkUtil;
import com.cjx.uibot.api.dto.LoginRequest;
import com.cjx.uibot.api.dto.TUserDto;
import com.cjx.uibot.service.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 钉钉登录控制器
 *
 * @author claude
 */
@Slf4j
@Tag(name = "钉钉登录")
@RestController
@RequestMapping("/auth/dingTalk")
@RequiredArgsConstructor
public class DingTalkController {
    private final DingTalkUtil dingTalkUtil;
    private final IUserService userService;

    /**
     * 获取JSAPI配置
     */
    @Operation(summary = "获取JSAPI配置")
    @GetMapping("/jsapi-config")
    public Result<Map<String, String>> getJsApiConfig(@RequestParam String url) {
        try {
            Map<String, String> config = dingTalkUtil.generateJsApiSignature(url);
            return Result.success(config);
        } catch (Exception e) {
            log.error("获取JSAPI配置失败", e);
            return Result.fail("获取配置失败");
        }
    }

    /**
     * 钉钉免密登录（移动端）
     */
    @Operation(summary = "钉钉免密登录")
    @PostMapping("/login")
    public Result<TUserDto> loginByAuthCode(@RequestBody LoginRequest request) {
        try {
            String authCode = request.getAuthCode();
            if (authCode == null || authCode.isEmpty()) {
                return Result.fail("授权码不能为空");
            }

            // 1. 通过authCode获取钉钉用户信息
            JSONObject dingUserInfo = dingTalkUtil.getUserInfoByAuthCode(authCode);
            String dingUserId = dingUserInfo.getStr("userid");

            log.info("钉钉用户ID: {}", dingUserId);

            // 2. 获取用户详细信息
            JSONObject userDetail = dingTalkUtil.getUserDetail(dingUserId);
            String mobile = userDetail.getStr("mobile");
            String name = userDetail.getStr("name");

            log.info("钉钉用户信息: mobile={}, name={}", mobile, name);

            // 3. 根据手机号或钉钉ID查询系统用户
            TUserDto response = userService.loginByDingTalk(dingUserId);

            if (response != null) {
                return Result.success(response);
            } else {
                return Result.fail("用户不存在，请先绑定账号");
            }

        } catch (Exception e) {
            log.error("钉钉登录失败", e);
            return Result.fail("登录失败: " + e.getMessage());
        }
    }

    /**
     * 钉钉扫码登录（PC端）
     */
//    @Operation(summary = "钉钉扫码登录")
//    @PostMapping("/qr-login")
//    public Result<TUserDto> loginByQRCode(@RequestBody Map<String, String> params) {
//        try {
//            String code = params.get("code");
//            if (code == null || code.isEmpty()) {
//                return Result.fail("授权码不能为空");
//            }
//
//            // 1. 通过扫码code获取用户信息
//            JSONObject userInfo = dingTalkUtil.getUserInfoByQRCode(code);
//            String openId = userInfo.getStr("openid");
//            String unionId = userInfo.getStr("unionid");
//
//            log.info("扫码用户信息: openId={}, unionId={}", openId, unionId);
//
//            // 2. 根据unionId查询系统用户
//            LoginResponse response = userService.loginByDingTalkUnionId(unionId);
//
//            if (response != null) {
//                return Result.success(response);
//            } else {
//                return Result.fail("用户不存在，请先在钉钉中登录绑定");
//            }
//
//        } catch (Exception e) {
//            log.error("扫码登录失败", e);
//            return Result.fail("登录失败: " + e.getMessage());
//        }
//    }

    /**
     * 绑定钉钉账号
     */
//    @Operation(summary = "绑定钉钉账号")
//    @PostMapping("/bind")
//    public Result<Void> bindDingTalk(@RequestBody Map<String, Object> params) {
//        try {
//            String authCode = (String) params.get("authCode");
//            Long userId = Long.valueOf(params.get("userId").toString());
//
//            // 获取钉钉用户信息
//            JSONObject dingUserInfo = dingTalkUtil.getUserInfoByAuthCode(authCode);
//            String dingUserId = dingUserInfo.getStr("userid");
//
//            // 绑定
//            boolean success = userService.bindDingTalk(userId, dingUserId);
//
//            return success ? Result.success() : Result.fail("绑定失败");
//
//        } catch (Exception e) {
//            log.error("绑定钉钉失败", e);
//            return Result.fail("绑定失败: " + e.getMessage());
//        }
//    }

    /**
     * 解绑钉钉账号
     */
//    @Operation(summary = "解绑钉钉账号")
//    @PostMapping("/unbind")
//    public Result<Void> unbindDingTalk(@RequestBody Map<String, Object> params) {
//        try {
//            Long userId = Long.valueOf(params.get("userId").toString());
//            boolean success = userService.unbindDingTalk(userId);
//            return success ? Result.success() : Result.fail("解绑失败");
//        } catch (Exception e) {
//            log.error("解绑钉钉失败", e);
//            return Result.fail("解绑失败: " + e.getMessage());
//        }
//    }
}
