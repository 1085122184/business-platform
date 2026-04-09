package com.cjx.decision.entity.frorcl;

/**
 * @author cuijixu
 */
public interface SalesReportProjection {
    String get物料();
    String get物料描述();
    String get物料组();
    String get物料组描述();
    String get渠道();
    Double get销量();
    Double get金额();
    Double get单价();
    String get客户名称();
    String get过账日期();
    String get工厂();

    String get总销量();

    String get总金额();
}
