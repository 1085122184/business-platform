package com.cjx.decision.repository.frorcl;

import com.cjx.decision.projection.frorcl.OrderDetail;
import com.cjx.decision.projection.frorcl.SalesSummary;
import com.cjx.decision.entity.frorcl.VLvlengRixiaoshou;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author cuijixu
 */
@Repository
public interface OrderRepository extends JpaRepository<VLvlengRixiaoshou, Long> {

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
}
