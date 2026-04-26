package com.cjx.decision.repository.frorcl;

import com.cjx.decision.projection.frorcl.OrderDetail;
import com.cjx.decision.entity.frorcl.VLvlengRixiaoshou;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

/**
 * 只读Repository，用于查询订单相关数据。
 * 仅提供查询方法，不支持增删改操作。
 *
 * @author cuijixu
 */
@org.springframework.stereotype.Repository
public interface OrderRepository extends Repository<VLvlengRixiaoshou, Long> {

    @Query(value = """
  SELECT 
    order_date AS "orderDate",
    order_no AS "orderNo",
    material_group AS "materialGroup",
    material_desc AS "materialDesc",
    delivery_status AS "deliveryStatus",
    sales_org AS "salesOrg",
    office AS "office",
    salesperson AS "salesPerson", 
    customer AS "customer",
    channel AS "channel"
  FROM v_order_detail 
  WHERE channel <> '公司间' 
    AND ORDER_DATE >= :thisMonth 
    AND ORDER_DATE < :lastMonth 
    AND SALES_ORG like '%'||:companyName||'%'
  """, nativeQuery = true)
    List<OrderDetail> findOrderDetail(@Param("thisMonth") String thisMonth, @Param("lastMonth") String lastMonth, @Param("companyName") String companyName);


    @Query(value = """
      select o.order_date AS "orderDate",
            o.order_no AS "orderNo",
            h.物料  AS "materialGroup",
            h.物料描述 AS "materialDesc",
           '' AS "deliveryStatus",
           '' AS "salesOrg",
           '' AS "office",
            '' AS "salesPerson",
            h.客户名称 AS "customer",
            order_num as "orderNum",
            round(order_amount,2) as "orderAmount",
             '' AS "channel",
            h.物料描述 AS materialDesc,h.销量 AS volume,h.金额*10000 AS amount,h.单价 AS price,h.办事处描述 AS office ,h.客户名称 AS customer,h.日期 AS detailDate from (
      select distinct(order_no) order_no ,order_date,customer,sum(order_num) order_num,sum(order_amount) order_amount from v_order_detail 
      where 1=1 
            and channel <> '公司间'
            AND ORDER_DATE >= :thisMonth
            AND ORDER_DATE < :lastMonth
            AND SALES_ORG like '%'||:companyName||'%'
      group by order_no ,order_date,customer
      ) o left JOIN dwm_v_jt_dingdan_fahuo h ON h.订单号 = o.ORDER_NO
  """, nativeQuery = true)
    List<Map<String, Object>> findOrderWithDetails(@Param("thisMonth") String thisMonth, @Param("lastMonth") String lastMonth, @Param("companyName") String companyName);
}
