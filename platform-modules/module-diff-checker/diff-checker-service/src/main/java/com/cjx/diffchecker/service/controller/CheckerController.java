package com.cjx.diffchecker.service.controller;

import com.cjx.common.core.exception.BusinessException;
import com.cjx.common.core.utils.BeanConverterUtil;
import com.cjx.common.dingtalk.utils.DingTalkUtil;
import com.cjx.diffchecker.service.service.CompareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author cuijixu
 */
@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Validated
@Tag(name = "数据差异检查", description = "比较数据是否一致")
@CrossOrigin(origins = "*")
public class CheckerController {
    private final CompareService compareService;
    @Resource(name = "ioExecutor")
    private ThreadPoolTaskExecutor ioExecutor;

    private final DingTalkUtil dingTalkUtil;

    @PostMapping("/srm/bip")
    @Operation(summary = "对比金额", description = "对比金额")
    public void test(@RequestBody Map<String,Object> request){
        ioExecutor.execute(()->{
            String pkInvoice = request.get("pkInvoice").toString();
            BigDecimal srmNtax = BeanConverterUtil.getBigDecimal(request,"ntax");
            BigDecimal srmNmny = BeanConverterUtil.getBigDecimal(request,"nmny");
            compareService.compareTotal(pkInvoice,srmNtax,srmNmny);
        });
    }
//

    @PostMapping("/send/{userId}")
    @Operation(summary = "钉钉消息发送", description = "钉钉消息发送")
    public void test(@RequestBody Map<String,Object> request, @PathVariable String userId){
        String message = request.get("message").toString();
        List<String> userIdList = List.of(userId.split(","));
        try {
            dingTalkUtil.sendTextMessage(userIdList,message);
        } catch (Exception e) {
            throw new BusinessException("发送消息失败",e);
        }
    }

}
