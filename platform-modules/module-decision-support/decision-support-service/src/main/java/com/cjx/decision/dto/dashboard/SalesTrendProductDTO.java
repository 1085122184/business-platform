package com.cjx.decision.dto.dashboard;

import com.cjx.decision.dto.dashboard.SalesTrendPointDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 量价趋势大盘 - 产品聚合对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesTrendProductDTO {
    /** 物理主键：物料编码 */
    private String productCode;
    /** 显示名称：物料名字 */
    private String product;
    /** 市场区域：如 '国内' / '国外' */
    private String region;
    /** 最新业务日期 (对应前端传进来的 date) */
    private String latestDate;
    /** 最新一日的销量 */
    private BigDecimal latestVolume;
    /** 最新一日的价格 */
    private BigDecimal latestPrice;
    /** 销量环比变动 (如 0.05 代表 5%, -0.02 代表 -2%) */
    private Double volumeChange;
    /** 价格环比变动 */
    private Double priceChange;
    /** 量价相关性系数 (可选计算，-1到1之间) */
    private Double correlation;
    /** 近期(如近15天/30天)的量价走势明细数组 */
    private List<SalesTrendPointDTO> trend;

    private List<SalesTrendPointDTO> trendYear;
}
