package com.cjx.diffchecker.service.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.cjx.common.core.exception.BusinessException;
import com.cjx.common.dingtalk.utils.DingTalkUtil;
import com.cjx.diffchecker.service.dto.InvoiceSummaryDTO;
import com.cjx.diffchecker.service.repository.bip.PoInvoiceBRepository;
import com.cjx.diffchecker.service.service.CompareService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cuijixu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompareServiceImpl implements CompareService {
    private final PoInvoiceBRepository poInvoiceBRepository;
    private final DingTalkUtil dingTalkUtil;
    @Value("${app.ding-talk.userId}")
    private String userId;
    @Override
    public void compareTotal(String pkInvoice,BigDecimal srmNtax,BigDecimal srmNmny) {
        InvoiceSummaryDTO poInvoiceB = poInvoiceBRepository.totalByPkInvoiceWithDto(pkInvoice);
        BigDecimal bipNyax = poInvoiceB.getTotalNtax();
        BigDecimal bipNmny = poInvoiceB.getTotalNmny();
        List<String> userIdList = new ArrayList<>();
        userIdList.add(userId);//DY03155
        if (!bipNyax.equals(srmNtax) || !bipNmny.equals(srmNmny)){
            try {
                String markdownContent = "数据不一致，BIP主键为：" +
                        pkInvoice + "\n\n" +
                        "srm本币税额为：" + srmNtax + "\n\n" +
                        "srm本币无税金额：" + srmNmny + "\n\n" +
                        LocalDateTimeUtil.format(LocalDateTimeUtil.now(), DatePattern.NORM_DATETIME_PATTERN);
                dingTalkUtil.sendMarkdownMessage(userIdList,"差异数据", markdownContent);
                log.info("数据不一致,钉钉发送消息成功");
            } catch (Exception e) {
                log.error("数据不一致,钉钉发送消息失败");
                throw new BusinessException("钉钉发送消息失败",e);
            }
        }else {
            log.info("数据一致");
        }
    }
}
