package com.cjx.decision.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderDetailDTO {

    private String orderDate;
    private String orderNo;
    private String materialDesc;
    private String deliveryStatus;
    private String salesOrg;
    private String office;
    private String materialGroup;
    private String salesPerson;
    private String customer;
    private String channel;
    private BigDecimal orderNum;
    private BigDecimal orderAmount;
    private List<OrderDetailItem> details;

    @Data
    public static class OrderDetailItem {
        @Schema(description = "金额")
        private BigDecimal amount;
        @Schema(description = "销量")
        private BigDecimal volume;
        @Schema(description = "单价")
        private BigDecimal price;
        @Schema(description = "公司")
        private String office;
        @Schema(description = "客户")
        private String customer;
        @Schema(description = "物料描述")
        private String materialDesc;
        @Schema(description = "发货日期")
        private String detailDate;
    }
}
