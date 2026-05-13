package com.cjx.decision.dto.dashboard;

import lombok.Data;

@Data
public class DashboardOrdersDTO {
    private RawOrder monthOrders;
    private RawOrder yearOrders;
}
