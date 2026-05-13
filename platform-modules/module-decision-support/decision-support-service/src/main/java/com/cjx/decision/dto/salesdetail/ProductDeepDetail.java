package com.cjx.decision.dto.salesdetail;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author cuijixu
 */
@Data
@NoArgsConstructor
public class ProductDeepDetail {

    private ProductDeepKPI kpi;
    private List<ProductDeepCustomer> topCustomers;
    private List<ProductDeepTrend> trend;

}
