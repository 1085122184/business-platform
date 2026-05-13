# 决策支持模块 SQL 映射文档

生成日期：2026-05-12

适用模块：`platform-modules/module-decision-support/decision-support-service`

## 1. 阅读说明

本文档按“入口接口/任务 -> Service 方法 -> Repository 方法 -> SQL”整理当前模块用到的 SQL。

说明：

1. “当前接口链路”表示从 Controller 可直接访问到的业务路径。
2. “系统鉴权链路”包含认证、角色、用户、菜单权限相关查询和 JPA 操作。
3. `SalesRepository` 已标记 `@Deprecated`，当前 Controller 链路未直接引用，但 `SalesServiceImpl` 和缓存预热仍引用它，因此放在附录。
4. Spring Data 派生查询、`save`、`delete`、`findById`、Specification 查询不会在源码中出现完整 SQL，文档按等价 SQL 口径说明。

## 2. 当前接口链路 SQL 总览

| 接口/功能 | Service 方法 | Repository 方法 | 数据对象 |
| --- | --- | --- | --- |
| `GET /api/metrics` | `MetricsServiceImpl.getMetrics` | `findSummaryByDate` | `v_sales_detail_all` |
| `GET /api/metrics` | `MetricsServiceImpl.getMetrics` | `findSummaryToToday` | `v_sales_detail_all` |
| `GET /api/metrics` | `MetricsServiceImpl.getMetrics` | `findCountBudget` | `V_SALES_BUDGET_SUMMARY` |
| `GET /api/metrics` | `MetricsServiceImpl.getMetrics` | `findAmountBudget` | `V_SALES_BUDGET_SUMMARY` |
| `GET /api/metrics` | `MetricsServiceImpl.getMetrics` | `findCollection` | 回款视图 |
| `GET /api/metrics/orders` | `MetricsServiceImpl.getOrders` | `findMonthOrder` | `so_order`、`v_yjg_order_qty` |
| `GET /api/metrics/orders` | `MetricsServiceImpl.getOrders` | `findYearOrder` | `so_order`、`v_yjg_order_qty` |
| `GET /api/price-analysis/deviations` | `PriceAnalysisServiceImpl.getPriceDeviations` | `findPriceDiff` | `v_sales_detail_all` |
| `GET /api/price-analysis/deviations/details` | `PriceAnalysisServiceImpl.getCustomerTransactions` | `findCustomerTransaction` | `v_sales_detail_all` |
| `GET /api/sales-analysis/companies` | `SalesAnalysisServiceImpl.getCompanyList` | `findCountBudgetDetails` | `V_SALES_BUDGET_SUMMARY` |
| `GET /api/sales-analysis/companies` | `SalesAnalysisServiceImpl.getCompanyList` | `findAmountBudgetDetails` | `V_SALES_BUDGET_SUMMARY` |
| `GET /api/sales-analysis/companies` | `SalesAnalysisServiceImpl.getCompanyList` | `findSummaryTodayDetails` | `v_sales_detail_all` |
| `GET /api/sales-analysis/companies/detail` | `SalesAnalysisServiceImpl.getCompanyDetail` | `findSummaryByCompany` | `v_sales_detail_all` |
| `GET /api/sales-analysis/companies/detail` | `SalesAnalysisServiceImpl.getCompanyDetail` | `findDetailsByCompany` | `v_sales_detail_all` |
| `GET /api/sales-analysis/orders/company-detail` | `OrderServiceImpl.getOrderWithDetails` | `findOrderWithDetails` | `v_order_detail`、`dwm_v_jt_dingdan_fahuo` |
| Service 未被 Controller 使用 | `OrderServiceImpl.getCompanyDetails` | `findOrderDetail` | `v_order_detail` |
| `GET /api/sales-analysis/product-deep?type=month` | `TrendAnalysisServiceImpl.getProductDeepDetail` | `getProductCustomerMonth` | `v_sales_detail_all` |
| `GET /api/sales-analysis/product-deep?type=month` | `TrendAnalysisServiceImpl.getProductDeepDetail` | `getProductDeepMonth` | `v_sales_detail_all` |
| `GET /api/sales-analysis/product-deep?type=year` | `TrendAnalysisServiceImpl.getProductDeepDetail` | `getProductCustomer` | `v_dwm_jt_fapiao_mingxi_std` |
| `GET /api/sales-analysis/product-deep?type=year` | `TrendAnalysisServiceImpl.getProductDeepDetail` | `getProductDeepYear` | `v_dwm_jt_fapiao_mingxi_std` |
| `GET /api/trend-analysis/monthly` | `TrendAnalysisServiceImpl.getMonthlyTrends` | `findTrendsAll` | `v_sales_detail_all` |
| `GET /api/trend-analysis/yearly` | `TrendAnalysisServiceImpl.getYearlyTrends` | `findTrendsYear` | `v_dwm_jt_fapiao_mingxi_std` |
| `GET /api/collection-analysis/companies` | `CollectionServiceImpl.getCollectionCompanies` | `findCollectionCompanies` | `V_HUIKUAN_ALL` |
| `GET /api/collection-analysis/companies` | `CollectionServiceImpl.getCollectionCompanies` | `findCollectionPlan` | `SO_PLANACCOUNT` |
| `GET /api/expense/overview` | `ExpenseServiceImpl.getOverview` | `getComparisonData` | `dwm_v_jt_sanfei` |
| `GET /api/expense/structure` | `ExpenseServiceImpl.getStructure` | 间接调用 `getOverview` | `dwm_v_jt_sanfei` |
| `GET /api/expense/trend` | `ExpenseServiceImpl.getTrend` | `getMonthlyTrend` | `dwm_v_jt_sanfei` |
| `GET /api/expense/company-comparison` | `ExpenseServiceImpl.getComparison` | `getCompanyComparison` | `dwm_v_jt_sanfei` |
| `GET /api/expense/company-detail` | `ExpenseServiceImpl.getCompanyDetail` | `getCompanyDetailsByPage` | `dwm_v_jt_sanfei` |
| `GET /api/expense/company-detail` | `ExpenseServiceImpl.getCompanyDetail` | `countCompanyDetails` | `dwm_v_jt_sanfei` |
| `GET /api/expense/budget-execution` | `ExpenseServiceImpl.getBudgetExecution` | `getBudget` | `YS_FEIYONG_JT` |
| `GET /api/expense/budget-execution` | `ExpenseServiceImpl.getBudgetExecution` | `getCompanyComparison` | `dwm_v_jt_sanfei` |
| `GET /api/expense/growth` | `ExpenseServiceImpl.getCompanyGrowthData` | `findCompanyMonthlySums` | `dwm_v_jt_sanfei` |
| Controller 未暴露 | `ExpenseServiceImpl.getDailyDetail` | `getDailyDetail` | `dwm_v_jt_sanfei_mingxi` |
| `GET /api/all_details/sale_details` | `AllDetailsServiceImpl.findSalesDetail` | `findSalesDetail` | `v_sales_detail_all` |

## 3. MetricsRepository

### 3.1 `MetricsServiceImpl.getMetrics` -> `MetricsRepository.findSummaryByDate`

用途：查询 `date - 1` 当日销量、销售额。

```sql
SELECT
  ROUND(SUM(NVL(销量, 0)), 2) AS totalSales,
  ROUND(SUM(NVL(金额, 0)), 2) AS totalAmount
FROM v_sales_detail_all
WHERE 过账日期 = :targetDate
  AND 销量 <> 0
```

参数：

| 参数 | 来源 |
| --- | --- |
| `targetDate` | `date.minusDays(1).format('yyyy-MM-dd')` |

### 3.2 `MetricsServiceImpl.getMetrics` -> `MetricsRepository.findSummaryToToday`

用途：查询 `date - 1` 所在月月初到 `date - 1` 的累计销量、销售额。

```sql
SELECT
  ROUND(SUM(NVL(销量, 0)), 2) AS totalSales,
  ROUND(SUM(NVL(金额, 0)), 2) AS totalAmount
FROM v_sales_detail_all
WHERE TO_DATE(过账日期, 'YYYY-MM-DD') >= TRUNC(TRUNC(:targetDate) - 1, 'MM')
  AND TO_DATE(过账日期, 'YYYY-MM-DD') < TRUNC(:targetDate)
  AND 销量 <> 0
```

参数：

| 参数 | 来源 |
| --- | --- |
| `targetDate` | 接口传入的 `date` |

### 3.3 `MetricsServiceImpl.getMetrics` -> `MetricsRepository.findCountBudget`

用途：查询月销量预算。

```sql
SELECT
  月份,
  ROUND(SUM(销量预算), 2) totalCountBudget
FROM V_SALES_BUDGET_SUMMARY
WHERE 月份 = :targetDate
GROUP BY 月份
```

参数：

| 参数 | 来源 |
| --- | --- |
| `targetDate` | `date.minusDays(1).format('yyyy-MM')` |

### 3.4 `MetricsServiceImpl.getMetrics` -> `MetricsRepository.findAmountBudget`

用途：查询月销售额预算。

```sql
SELECT
  月份,
  ROUND(SUM(销售额预算), 2) totalAmountBudget
FROM V_SALES_BUDGET_SUMMARY
WHERE 月份 = :targetDate
GROUP BY 月份
```

### 3.5 `MetricsServiceImpl.getMetrics` -> `MetricsRepository.findCollection`

用途：查询当月回款总额。

```sql
SELECT SUM(回款) AS collection
FROM (
  SELECT SUM(回款) AS 回款, '绿冷' AS 公司
  FROM v_lvleng_huikuan
  WHERE 渠道 <> '关联'
    AND 年度 = TO_CHAR(:targetDate, 'yyyy')
    AND 期间 = TO_CHAR(:targetDate, 'MM')
  UNION ALL
  SELECT SUM(回款) AS 回款, '高分子' AS 公司
  FROM v_gfz_huikuan
  WHERE 渠道 <> '关联'
    AND 财年 = TO_CHAR(:targetDate, 'yyyy')
    AND 过账期间 = TO_CHAR(:targetDate, 'MM')
  UNION ALL
  SELECT SUM(回款) AS 回款, '氟硅' AS 公司
  FROM v_fg_huikuan
  WHERE 渠道 <> '关联'
    AND 财年 = TO_CHAR(:targetDate, 'yyyy')
    AND 过账期间 = TO_CHAR(:targetDate, 'MM')
  UNION ALL
  SELECT SUM(回款) AS 回款, '有机硅' AS 公司
  FROM V_YJG_HUIKUAN
  WHERE 渠道 <> '关联'
    AND TO_CHAR(时间戳, 'yyyy-MM') = TO_CHAR(:targetDate, 'yyyy-MM')
)
```

参数：`targetDate = date.minusDays(1)`。

### 3.6 `MetricsServiceImpl.getOrders` -> `MetricsRepository.findMonthOrder`

用途：查询本月订单、关闭订单、未关闭订单。

```sql
SELECT
  SUM(总订单数) AS totalOrder,
  SUM(关闭订单数) AS closedOrder,
  SUM(非关闭状态订单数) AS openOrder
FROM (
  SELECT
    COUNT(1) AS 总订单数,
    COUNT(CASE WHEN 交货状态 = '完全处理' THEN 1 END) AS 关闭订单数,
    COUNT(1) - COUNT(CASE WHEN 交货状态 = '完全处理' THEN 1 END) AS 非关闭状态订单数
  FROM so_order
  WHERE 创建日期 >= :thisMonth
    AND 创建日期 < :lastMonth
    AND 工厂 IN ('1301', '1201', '3001')

  UNION ALL

  SELECT
    COUNT(1) AS 总订单数,
    COUNT(CASE WHEN 单据状态 = '关闭' THEN 1 END) AS 关闭订单数,
    COUNT(*) - COUNT(CASE WHEN 单据状态 = '关闭' THEN 1 END) AS 非关闭状态订单数
  FROM v_yjg_order_qty
  WHERE 公司编码 = '1400'
    AND TO_CHAR(时间戳, 'YYYY-MM-DD') = TO_CHAR(TRUNC(:targetDate), 'YYYY-MM-DD')
)
```

参数：

| 参数 | 来源 |
| --- | --- |
| `thisMonth` | `targetDate.minusDays(1).withDayOfMonth(1)` |
| `lastMonth` | `targetDate.minusDays(1).with(lastDayOfMonth())` |
| `targetDate` | 接口传入日期 |

### 3.7 `MetricsServiceImpl.getOrders` -> `MetricsRepository.findYearOrder`

用途：查询本年订单、关闭订单、未关闭订单。

```sql
WITH daily_status AS (
  SELECT
    TO_CHAR(时间戳, 'YYYY-MM-DD') AS 创建日期,
    单据状态,
    COUNT(*) AS 总订单数
  FROM v_yjg_order_qty
  WHERE TO_CHAR(时间戳, 'YYYY-MM-DD') >= SUBSTR(:targetDate, 1, 4)
    AND TO_CHAR(时间戳, 'YYYY-MM-DD') <= :targetDate
    AND 公司编码 = '1400'
  GROUP BY TO_CHAR(时间戳, 'YYYY-MM-DD'), 单据状态
),
monthly_last AS (
  SELECT
    单据状态,
    总订单数,
    ROW_NUMBER() OVER (
      PARTITION BY 单据状态, SUBSTR(创建日期, 6, 2)
      ORDER BY 创建日期 DESC
    ) AS rn
  FROM daily_status
)
SELECT
  SUM(总订单数) AS totalOrder,
  SUM(关闭订单数) AS closedOrder,
  SUM(非关闭状态订单数) AS openOrder
FROM (
  SELECT
    COUNT(*) AS 总订单数,
    COUNT(CASE WHEN 交货状态 = '完全处理' THEN 1 END) AS 关闭订单数,
    COUNT(*) - COUNT(CASE WHEN 交货状态 = '完全处理' THEN 1 END) AS 非关闭状态订单数
  FROM so_order
  WHERE 创建日期 >= :thisYear
    AND 工厂 IN ('1301', '1201', '3001')

  UNION ALL

  SELECT
    SUM(总订单数) AS 总订单数,
    SUM(CASE WHEN 单据状态 = '关闭' THEN 总订单数 ELSE 0 END) AS 关闭订单数,
    SUM(总订单数) - SUM(CASE WHEN 单据状态 = '关闭' THEN 总订单数 ELSE 0 END) AS 非关闭状态订单数
  FROM monthly_last
  WHERE rn = 1
)
```

## 4. PriceAnalysisRepository

### 4.1 `PriceAnalysisServiceImpl.getPriceDeviations` -> `PriceAnalysisRepository.findPriceDiff`

用途：查询近 7 日均价和昨日价格偏差。

```sql
SELECT
  t.物料组 AS productCode,
  t.物料组描述 AS productName,
  CASE t.渠道
    WHEN '10' THEN '国内'
    WHEN '20' THEN '国外'
  END AS region,
  t.七日均价 AS avgPrice7d,
  t.当日单价 AS todayPrice,
  t.价差偏移 AS deviationAmt,
  CASE
    WHEN t.七日均价 = 0 OR t.七日均价 IS NULL THEN 0
    ELSE ROUND((t.当日单价 - t.七日均价) / t.七日均价 * 100, 2)
  END AS deviationPct
FROM (
  SELECT
    工厂,
    物料组,
    物料组描述,
    渠道,
    ROUND(SUM(金额) / NULLIF(SUM(销量), 0) * 10000, 2) AS 七日均价,
    ROUND(
      SUM(CASE WHEN 过账日期 = :endDate THEN 金额 ELSE 0 END)
      / NULLIF(SUM(CASE WHEN 过账日期 = :endDate THEN 销量 ELSE 0 END), 0)
      * 10000,
      2
    ) AS 当日单价,
    ROUND(
      SUM(CASE WHEN 过账日期 = :endDate THEN 金额 ELSE 0 END)
      / NULLIF(SUM(CASE WHEN 过账日期 = :endDate THEN 销量 ELSE 0 END), 0)
      * 10000,
      2
    ) - ROUND(SUM(金额) / NULLIF(SUM(销量), 0) * 10000, 2) AS 价差偏移
  FROM v_sales_detail_all
  WHERE 物料组描述 <> '无价值物料'
    AND 销售主产 = '1'
    AND 过账日期 BETWEEN :startDate AND :endDate
  GROUP BY 工厂, 物料组, 物料组描述, 渠道
) t
WHERE 当日单价 IS NOT NULL
ORDER BY deviationPct
```

参数：

| 参数 | 来源 |
| --- | --- |
| `startDate` | `date.minusDays(7).format('yyyy-MM-dd')` |
| `endDate` | `date.minusDays(1).format('yyyy-MM-dd')` |

### 4.2 `PriceAnalysisServiceImpl.getCustomerTransactions` -> `PriceAnalysisRepository.findCustomerTransaction`

用途：查询某产品、区域、时间段内的客户成交量价。

```sql
SELECT
  SUM(销量) AS volume,
  ROUND(SUM(金额 * 10000) / SUM(销量), 2) AS price,
  客户名称 AS customer
FROM v_sales_detail_all
WHERE 过账日期 >= :beginDate
  AND 过账日期 <= :endDate
  AND 渠道 = :region
  AND 物料组 = :code
  AND 物料组描述 <> '无价值物料'
  AND 销量 <> 0
GROUP BY 客户名称
```

参数：

| 参数 | 来源 |
| --- | --- |
| `region` | `RegionCode.getCodeByName(region)`，国内为 `10`，国外为 `20` |
| `code` | 接口参数 `code` |
| `beginDate` | `type=7days` 时为服务器当前日期 - 7，否则为服务器当前日期 - 1 |
| `endDate` | 服务器当前日期 - 1 |

## 5. SalesAnalysisRepository

### 5.1 `SalesAnalysisServiceImpl.getCompanyList` -> `findCountBudgetDetails`

用途：查询各公司销量预算。

```sql
SELECT
  月份,
  公司编码 AS companyName,
  ROUND(SUM(销量预算), 2) totalCountBudget
FROM V_SALES_BUDGET_SUMMARY
WHERE 月份 = :targetDate
GROUP BY 月份, 公司编码
```

### 5.2 `SalesAnalysisServiceImpl.getCompanyList` -> `findAmountBudgetDetails`

用途：查询各公司销售额预算。

```sql
SELECT
  月份,
  公司编码 AS companyName,
  ROUND(SUM(销售额预算), 2) totalAmountBudget
FROM V_SALES_BUDGET_SUMMARY
WHERE 月份 = :targetDate
GROUP BY 月份, 公司编码
```

### 5.3 `SalesAnalysisServiceImpl.getCompanyList` -> `findSummaryTodayDetails`

用途：查询各公司月累计销量、销售额。

```sql
SELECT
  CASE 工厂
    WHEN '1201' THEN '高分子'
    WHEN '1301' THEN '氟硅'
    WHEN '3001' THEN '绿冷'
    WHEN '1400' THEN '有机硅'
  END AS companyName,
  公司编码 AS companyCode,
  ROUND(SUM(NVL(销量, 0)), 2) AS totalSales,
  ROUND(SUM(NVL(金额, 0)), 2) AS totalAmount
FROM v_sales_detail_all
WHERE TO_DATE(过账日期, 'YYYY-MM-DD') >= TRUNC(TRUNC(:targetDate) - 1, 'MM')
  AND TO_DATE(过账日期, 'YYYY-MM-DD') < TRUNC(:targetDate)
  AND 工厂 IS NOT NULL
  AND 销量 <> 0
GROUP BY 工厂, 公司编码
```

### 5.4 `SalesAnalysisServiceImpl.getCompanyDetail` -> `findSummaryByCompany`

用途：查询公司本月每日销量/销售额趋势，空日期补 0。

```sql
WITH date_range AS (
  SELECT TO_CHAR(TRUNC(:targetDate - 1, 'MM') + LEVEL - 1, 'YYYY-MM-DD') AS dt
  FROM DUAL
  CONNECT BY LEVEL <= TRUNC(:targetDate) - TRUNC(:targetDate - 1, 'MM')
),
agg AS (
  SELECT
    过账日期,
    ROUND(SUM(NVL(销量, 0)), 2) AS totalSales,
    ROUND(SUM(NVL(金额, 0)), 2) AS totalAmount
  FROM v_sales_detail_all
  WHERE 公司编码 = :company
    AND TO_DATE(过账日期, 'YYYY-MM-DD') >= TRUNC(:targetDate - 1, 'MM')
    AND TO_DATE(过账日期, 'YYYY-MM-DD') <= TRUNC(:targetDate - 1)
  GROUP BY 过账日期
)
SELECT
  d.dt AS 过账日期,
  NVL(a.totalSales, 0) AS totalSales,
  NVL(a.totalAmount, 0) AS totalAmount
FROM date_range d
LEFT JOIN agg a ON d.dt = a.过账日期
ORDER BY d.dt
```

### 5.5 `SalesAnalysisServiceImpl.getCompanyDetail` -> `findDetailsByCompany`

用途：查询公司 `date - 1` 当天的产品明细及占比。

```sql
SELECT
  物料组描述 AS productName,
  物料组 AS productCode,
  ROUND(SUM(NVL(销量, 0)), 2) AS totalSales,
  ROUND(SUM(NVL(金额, 0)), 2) AS totalAmount,
  ROUND(
    SUM(NVL(销量, 0)) / NULLIF(SUM(SUM(NVL(销量, 0))) OVER(), 0) * 100,
    2
  ) AS salesRatio,
  ROUND(
    SUM(NVL(金额, 0)) / NULLIF(SUM(SUM(NVL(金额, 0))) OVER(), 0) * 100,
    2
  ) AS amountRatio,
  CASE 渠道 WHEN '10' THEN '国内' WHEN '20' THEN '国外' END AS region
FROM v_sales_detail_all
WHERE 公司编码 = :company
  AND TO_DATE(过账日期, 'YYYY-MM-DD') = TRUNC(:targetDate - 1)
  AND 物料组描述 <> '无价值物料'
  AND 销售主产 = '1'
GROUP BY 渠道, 物料组描述, 物料组
```

## 6. OrderRepository

### 6.1 `OrderServiceImpl.getCompanyDetails` -> `OrderRepository.findOrderDetail`

用途：查询订单主信息。当前 Controller 未直接调用该 Service 方法。

```sql
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
  AND SALES_ORG LIKE '%' || :companyName || '%'
```

### 6.2 `OrderServiceImpl.getOrderWithDetails` -> `OrderRepository.findOrderWithDetails`

用途：查询订单主信息并关联发货明细。

```sql
SELECT
  o.order_date AS "orderDate",
  o.order_no AS "orderNo",
  h.物料 AS "materialGroup",
  h.物料描述 AS "materialDesc",
  '' AS "deliveryStatus",
  '' AS "salesOrg",
  '' AS "office",
  '' AS "salesPerson",
  h.客户名称 AS "customer",
  order_num AS "orderNum",
  ROUND(order_amount, 2) AS "orderAmount",
  '' AS "channel",
  h.物料描述 AS materialDesc,
  h.销量 AS volume,
  h.金额 * 10000 AS amount,
  h.单价 AS price,
  h.办事处描述 AS office,
  h.客户名称 AS customer,
  h.日期 AS detailDate
FROM (
  SELECT
    DISTINCT(order_no) order_no,
    order_date,
    customer,
    SUM(order_num) order_num,
    SUM(order_amount) order_amount
  FROM v_order_detail
  WHERE 1 = 1
    AND channel <> '公司间'
    AND ORDER_DATE >= :thisMonth
    AND ORDER_DATE < :lastMonth
    AND SALES_ORG LIKE '%' || :companyName || '%'
  GROUP BY order_no, order_date, customer
) o
LEFT JOIN dwm_v_jt_dingdan_fahuo h ON h.订单号 = o.ORDER_NO
```

## 7. TrendAnalysisRepository

### 7.1 `TrendAnalysisServiceImpl.getMonthlyTrends` -> `findTrendsAll`

用途：查询近 30 天全产品量价趋势。

```sql
SELECT
  工厂,
  CASE 渠道
    WHEN '10' THEN '国内'
    WHEN '20' THEN '国外'
  END AS region,
  物料组 AS productCode,
  物料组描述 AS productName,
  过账日期 AS latestDate,
  ROUND(SUM(NVL(销量, 0)), 2) AS totalSales,
  ROUND(SUM(NVL(金额, 0)), 2) AS totalAmount,
  ROUND(
    CASE WHEN SUM(NVL(销量, 0)) = 0 THEN 0 ELSE SUM(NVL(金额, 0)) / SUM(NVL(销量, 0)) * 10000 END,
    2
  ) AS price
FROM v_sales_detail_all
WHERE 过账日期 BETWEEN :startDate AND :endDate
  AND 物料组描述 <> '无价值物料'
  AND 销售主产 = '1'
GROUP BY 工厂, 渠道, 物料组, 物料组描述, 过账日期
```

### 7.2 `TrendAnalysisServiceImpl.getYearlyTrends` -> `findTrendsYear`

用途：查询指定产品、区域的年度月度量价趋势。

```sql
SELECT
  公司编码,
  渠道 AS region,
  物料组 AS productCode,
  物料组描述 AS productName,
  TO_CHAR(TO_DATE(日期, 'YYYY-MM-DD'), 'YYYY-MM') AS latestDate,
  ROUND(SUM(NVL(开票量, 0)), 2) AS totalSales,
  ROUND(SUM(NVL(开票金额, 0)), 2) AS totalAmount,
  ROUND(
    CASE WHEN SUM(NVL(开票量, 0)) = 0 THEN 0 ELSE SUM(NVL(开票金额, 0)) / SUM(NVL(开票量, 0)) * 10000 END,
    2
  ) AS price
FROM v_dwm_jt_fapiao_mingxi_std
WHERE 物料组 = :productCode
  AND 渠道 = :region
  AND 物料组描述 <> '无价值物料'
  AND 日期 BETWEEN :startDate AND :endDate
GROUP BY 公司编码, 渠道, 物料组, 物料组描述, TO_CHAR(TO_DATE(日期, 'YYYY-MM-DD'), 'YYYY-MM')
ORDER BY TO_CHAR(TO_DATE(日期, 'YYYY-MM-DD'), 'YYYY-MM')
```

### 7.3 `TrendAnalysisServiceImpl.getProductDeepDetail(type=month)` -> `getProductDeepMonth`

用途：查询产品月度深度趋势。

```sql
SELECT
  渠道 AS region,
  物料组 AS productCode,
  物料组描述 AS productName,
  SUM(销量) AS totalSales,
  SUM(金额) AS totalAmount,
  过账日期 AS latestDate
FROM v_sales_detail_all
WHERE 公司编码 = :companyName
  AND 物料组 = :productCode
  AND TO_DATE(过账日期, 'YYYY-MM-DD') >= TRUNC(:targetDate - 1, 'MM')
  AND TO_DATE(过账日期, 'YYYY-MM-DD') <= TRUNC(:targetDate - 1)
GROUP BY 渠道, 物料组, 物料组描述, 过账日期
ORDER BY 过账日期
```

### 7.4 `TrendAnalysisServiceImpl.getProductDeepDetail(type=year)` -> `getProductDeepYear`

用途：查询产品年度深度趋势。

```sql
SELECT
  CASE WHEN 渠道 = '国内' THEN '10' WHEN 渠道 = '国外' THEN '20' ELSE '30' END AS region,
  物料组 AS productCode,
  物料组描述 AS productName,
  SUM(开票量) AS totalSales,
  SUM(开票金额) AS totalAmount,
  SUBSTR(日期, 1, 7) AS latestDate
FROM v_dwm_jt_fapiao_mingxi_std
WHERE 公司编码 = :companyName
  AND 物料组 = :productCode
  AND 渠道 <> '关联'
  AND SUBSTR(日期, 1, 4) = TO_CHAR(TRUNC(:targetDate - 1), 'YYYY')
GROUP BY 渠道, 物料组, 物料组描述, SUBSTR(日期, 1, 7)
ORDER BY SUBSTR(日期, 1, 7)
```

### 7.5 `TrendAnalysisServiceImpl.getProductDeepDetail(type=year)` -> `getProductCustomer`

用途：查询产品本年客户购买排行。

```sql
SELECT
  客户名称 AS customer,
  渠道 AS region,
  物料组 AS productCode,
  物料组描述 AS productName,
  SUM(开票量) AS totalSales,
  SUM(开票金额) AS totalAmount
FROM v_dwm_jt_fapiao_mingxi_std
WHERE 公司编码 = :companyName
  AND 物料组 = :productCode
  AND 渠道 <> '关联'
  AND SUBSTR(日期, 1, 4) = TO_CHAR(TRUNC(:targetDate - 1), 'YYYY')
GROUP BY 客户名称, 渠道, 物料组, 物料组描述
ORDER BY totalSales DESC
```

### 7.6 `TrendAnalysisServiceImpl.getProductDeepDetail(type=month)` -> `getProductCustomerMonth`

用途：查询产品本月客户购买排行。

```sql
SELECT
  客户名称 AS customer,
  渠道 AS region,
  物料组 AS productCode,
  物料组描述 AS productName,
  SUM(销量) AS totalSales,
  SUM(金额) AS totalAmount
FROM v_sales_detail_all
WHERE 公司编码 = :companyName
  AND 物料组 = :productCode
  AND 渠道 <> '关联'
  AND TO_DATE(过账日期, 'YYYY-MM-DD') >= TRUNC(:targetDate - 1, 'MM')
  AND TO_DATE(过账日期, 'YYYY-MM-DD') <= TRUNC(:targetDate - 1)
GROUP BY 客户名称, 渠道, 物料组, 物料组描述
ORDER BY totalSales DESC
```

## 8. CollectionRepository

### 8.1 `CollectionServiceImpl.getCollectionCompanies` -> `findCollectionCompanies`

用途：查询各公司回款实际值。

```sql
SELECT
  CASE 公司编码
    WHEN '1200' THEN '高分子'
    WHEN '1300' THEN '氟硅'
    WHEN '3000' THEN '绿冷'
    WHEN '1400' THEN '有机硅'
  END AS companyName,
  SUM(回款) AS value,
  SN,
  会计期间 AS yesterday
FROM V_HUIKUAN_ALL
WHERE 会计期间 = :targetDate
GROUP BY 公司编码, SN, 会计期间
ORDER BY SN
```

### 8.2 `CollectionServiceImpl.getCollectionCompanies` -> `findCollectionPlan`

用途：查询各公司回款计划值。

```sql
SELECT
  COMPANY AS companyName,
  PLANACCOUNTS AS planValue,
  CHECKYEAR || '-' || CHECKMONTH AS yesterday
FROM SO_PLANACCOUNT
WHERE CHECKYEAR || '-' || CHECKMONTH = :targetDate
```

## 9. ExpenseRepository

### 9.1 `ExpenseServiceImpl.getOverview` -> `getComparisonData`

用途：查询当前月和上月三费对比。

```sql
SELECT
  SUM(CASE WHEN 月份 = :curr AND 项目 LIKE '%销售费用%' THEN 期末余额 ELSE 0 END) AS CUR_SALES,
  SUM(CASE WHEN 月份 = :last AND 项目 LIKE '%销售费用%' THEN 期末余额 ELSE 0 END) AS LST_SALES,
  SUM(CASE WHEN 月份 = :curr AND 项目 LIKE '%管理费用%' THEN 期末余额 ELSE 0 END) AS CUR_MANAGE,
  SUM(CASE WHEN 月份 = :last AND 项目 LIKE '%管理费用%' THEN 期末余额 ELSE 0 END) AS LST_MANAGE,
  SUM(CASE WHEN 月份 = :curr AND 项目 LIKE '%财务费用%' THEN 期末余额 ELSE 0 END) AS CUR_FINANCE,
  SUM(CASE WHEN 月份 = :last AND 项目 LIKE '%财务费用%' THEN 期末余额 ELSE 0 END) AS LST_FINANCE
FROM dwm_v_jt_sanfei
WHERE 月份 IN (:curr, :last)
```

### 9.2 `ExpenseServiceImpl.getTrend` -> `getMonthlyTrend`

用途：查询三费月度趋势。

```sql
SELECT
  月份 AS MONTH_STR,
  SUM(CASE WHEN 项目 LIKE '%销售费用%' THEN 期末余额 ELSE 0 END) AS SALES,
  SUM(CASE WHEN 项目 LIKE '%管理费用%' THEN 期末余额 ELSE 0 END) AS MANAGE,
  SUM(CASE WHEN 项目 LIKE '%财务费用%' THEN 期末余额 ELSE 0 END) AS FINANCE
FROM dwm_v_jt_sanfei
WHERE 月份 BETWEEN :startDate AND :endDate
GROUP BY 月份
ORDER BY MONTH_STR ASC
```

### 9.3 `ExpenseServiceImpl.getCompanyDetail` -> `getCompanyDetailsByPage`

用途：分页查询公司三费明细。

```sql
SELECT COMPANY_NAME, SALES_EXP, MANAGE_EXP, FINANCE_EXP
FROM (
  SELECT t.*, ROWNUM rn
  FROM (
    SELECT
      公司名称 AS COMPANY_NAME,
      SUM(CASE WHEN 项目 LIKE '%销售费用%' THEN 期末余额 ELSE 0 END) AS SALES_EXP,
      SUM(CASE WHEN 项目 LIKE '%管理费用%' THEN 期末余额 ELSE 0 END) AS MANAGE_EXP,
      SUM(CASE WHEN 项目 LIKE '%财务费用%' THEN 期末余额 ELSE 0 END) AS FINANCE_EXP
    FROM dwm_v_jt_sanfei
    WHERE 月份 = :date
      AND (:keyword IS NULL OR 公司名称 LIKE '%' || :keyword || '%')
    GROUP BY 公司名称
  ) t
)
WHERE rn >= :offset
  AND rn <= (:offset + :pageSize)
```

### 9.4 `ExpenseServiceImpl.getCompanyDetail` -> `countCompanyDetails`

用途：查询公司三费明细总数。

```sql
SELECT COUNT(DISTINCT 公司名称)
FROM dwm_v_jt_sanfei
WHERE 月份 = :date
  AND (:keyword IS NULL OR 公司名称 LIKE '%' || :keyword || '%')
```

### 9.5 `ExpenseServiceImpl.getComparison` / `getBudgetExecution` -> `getCompanyComparison`

用途：按公司汇总三费实际值。`getComparison` 传当前月到当前月；`getBudgetExecution` 可传当前月或年初到当前月。

```sql
SELECT
  公司名称 AS COMPANY_NAME,
  SUM(CASE WHEN 项目 LIKE '%销售费用%' THEN 期末余额 ELSE 0 END) AS SALES,
  SUM(CASE WHEN 项目 LIKE '%管理费用%' THEN 期末余额 ELSE 0 END) AS MANAGE,
  SUM(CASE WHEN 项目 LIKE '%财务费用%' THEN 期末余额 ELSE 0 END) AS FINANCE
FROM dwm_v_jt_sanfei
WHERE 月份 BETWEEN :startDay AND :thisDay
GROUP BY 公司名称
ORDER BY SUM(期末余额) DESC
```

### 9.6 `ExpenseServiceImpl.getDailyDetail` -> `getDailyDetail`

用途：查询三费每日明细。当前 Controller 未暴露该接口。

```sql
SELECT
  公司名称 COMPANY_NAME,
  费用类型 TYPES,
  本币金额 * 10000 AMOUNT,
  行项目文本 TEXT
FROM dwm_v_jt_sanfei_mingxi
WHERE 过账日期 = :date
  AND 公司名称 = :companyName
```

### 9.7 `ExpenseServiceImpl.getBudgetExecution` -> `getBudget`

用途：查询三费预算。

```sql
SELECT
  公司 AS COMPANY_NAME,
  SUM(销售费用) AS SALES_BUDGET,
  SUM(管理费用) AS MANAGE_BUDGET,
  SUM(财务费用) AS FINANCE_BUDGET
FROM YS_FEIYONG_JT
WHERE 月份 BETWEEN :startDay AND :thisDay
GROUP BY 公司
```

### 9.8 `ExpenseServiceImpl.getCompanyGrowthData` -> `findCompanyMonthlySums`

用途：查询指定月份各公司三费总额。该方法会分别查当前月、去年同期、上月。

```sql
SELECT
  公司名称 AS companyName,
  SUM(期末余额) AS totalAmount
FROM dwm_v_jt_sanfei
WHERE 月份 = :month
GROUP BY 公司名称
```

### 9.9 当前未使用：`findExpenseOverviewByMonth`

源码存在但当前 Service 未调用。

```sql
SELECT
  项目 AS expenseType,
  SUM(期末余额) AS totalAmount
FROM dwm_v_jt_sanfei
WHERE 月份 = :month
GROUP BY 项目
```

## 10. AllDetailsRepository

### 10.1 `AllDetailsServiceImpl.findSalesDetail` -> `findSalesDetail`

用途：查询销售明细。

```sql
SELECT
  过账日期 businessDate,
  CASE 工厂
    WHEN '1201' THEN '高分子'
    WHEN '1301' THEN '氟硅'
    WHEN '3001' THEN '绿冷'
    WHEN '1400' THEN '有机硅'
  END AS companyName,
  CASE 渠道
    WHEN '10' THEN '国内'
    WHEN '20' THEN '国外'
  END AS region,
  物料描述 AS productName,
  物料组描述 AS groupName,
  销量 AS sales,
  金额 AS amount,
  ROUND(金额 * 10000 / 销量, 2) AS price
FROM v_sales_detail_all
WHERE 过账日期 = :targetDate
  AND (:companyCode IS NULL OR :companyCode = '' OR 公司编码 = :companyCode)
```

## 11. 系统鉴权和角色用户 SQL

### 11.1 `SystemAuthServiceImpl.login` -> `SysUserRepository.findByUsernameAndDelFlag`

Spring Data 派生查询，等价 SQL：

```sql
SELECT *
FROM SYS_USER
WHERE USERNAME = :username
  AND DEL_FLAG = :delFlag
FETCH FIRST 1 ROWS ONLY
```

登录成功或失败会写入 `SYS_LOGIN_LOG`，成功还会更新 `SYS_USER.LAST_LOGIN_TIME`、`SYS_USER.LAST_LOGIN_IP`。

### 11.2 `SystemAuthServiceImpl.loginByDingTalk` -> `findByDingUserIdAndDelFlag`

等价 SQL：

```sql
SELECT *
FROM SYS_USER
WHERE DING_USER_ID = :dingUserId
  AND DEL_FLAG = :delFlag
FETCH FIRST 1 ROWS ONLY
```

如果按钉钉 ID 未找到，会尝试手机号绑定：

```sql
SELECT *
FROM SYS_USER
WHERE MOBILE = :mobile
  AND DEL_FLAG = :delFlag
FETCH FIRST 1 ROWS ONLY
```

### 11.3 `SystemAuthServiceImpl.consumeLoginTicket` / `getProfile` -> `findByIdAndDelFlag`

等价 SQL：

```sql
SELECT *
FROM SYS_USER
WHERE USER_ID = :id
  AND DEL_FLAG = :delFlag
FETCH FIRST 1 ROWS ONLY
```

### 11.4 `SystemAuthServiceImpl.buildLoginResponse` / `getProfile` -> `SysRoleRepository.findRoleKeysByUserId`

用途：查询当前用户角色 key。

```sql
SELECT R.ROLE_KEY
FROM SYS_USER_ROLE UR
JOIN SYS_ROLE R ON R.ROLE_ID = UR.ROLE_ID
WHERE UR.USER_ID = :userId
  AND R.STATUS = 1
ORDER BY R.ROLE_SORT ASC, R.ROLE_ID ASC
```

### 11.5 `SystemAuthServiceImpl.buildLoginResponse` / `getProfile` -> `SysMenuRepository.findPermissionsByUserId`

用途：查询当前用户权限点。

```sql
SELECT DISTINCT M.PERMS
FROM SYS_USER_ROLE UR
JOIN SYS_ROLE_MENU RM ON RM.ROLE_ID = UR.ROLE_ID
JOIN SYS_MENU M ON M.MENU_ID = RM.MENU_ID
JOIN SYS_ROLE R ON R.ROLE_ID = UR.ROLE_ID
WHERE UR.USER_ID = :userId
  AND R.STATUS = 1
  AND M.STATUS = 1
  AND M.PERMS IS NOT NULL
ORDER BY M.PERMS
```

### 11.6 `SystemRoleServiceImpl.pageRoles`

实现方式：`JpaSpecificationExecutor` 动态条件查询。

等价条件：

```sql
SELECT *
FROM SYS_ROLE
WHERE (:roleName IS NULL OR ROLE_NAME LIKE '%' || :roleName || '%')
  AND (:roleKey IS NULL OR ROLE_KEY LIKE '%' || :roleKey || '%')
  AND (:status IS NULL OR STATUS = :status)
ORDER BY ROLE_SORT ASC, ROLE_ID ASC
OFFSET :offset ROWS FETCH NEXT :pageSize ROWS ONLY
```

### 11.7 `SystemRoleServiceImpl.pageUsers`

实现方式：`JpaSpecificationExecutor` 动态条件查询。

等价条件：

```sql
SELECT *
FROM SYS_USER
WHERE DEL_FLAG = '0'
  AND (:username IS NULL OR USERNAME LIKE '%' || :username || '%')
  AND (:mobile IS NULL OR MOBILE LIKE '%' || :mobile || '%')
  AND (:status IS NULL OR STATUS = :status)
ORDER BY USER_ID DESC
OFFSET :offset ROWS FETCH NEXT :pageSize ROWS ONLY
```

### 11.8 角色和用户唯一性校验

`SysRoleRepository.existsByRoleKey`：

```sql
SELECT COUNT(1)
FROM SYS_ROLE
WHERE ROLE_KEY = :roleKey
```

`SysRoleRepository.existsByRoleKeyAndIdNot`：

```sql
SELECT COUNT(1)
FROM SYS_ROLE
WHERE ROLE_KEY = :roleKey
  AND ROLE_ID <> :id
```

`SysUserRepository.existsByUsernameAndDelFlag`：

```sql
SELECT COUNT(1)
FROM SYS_USER
WHERE USERNAME = :username
  AND DEL_FLAG = :delFlag
```

`SysUserRepository.existsByMobileAndDelFlag`：

```sql
SELECT COUNT(1)
FROM SYS_USER
WHERE MOBILE = :mobile
  AND DEL_FLAG = :delFlag
```

`SysUserRepository.existsByMobileAndDelFlagAndIdNot`：

```sql
SELECT COUNT(1)
FROM SYS_USER
WHERE MOBILE = :mobile
  AND DEL_FLAG = :delFlag
  AND USER_ID <> :id
```

### 11.9 菜单树和授权

`SysMenuRepository.findByStatusOrderByOrderNumAscIdAsc`：

```sql
SELECT *
FROM SYS_MENU
WHERE STATUS = :status
ORDER BY ORDER_NUM ASC, MENU_ID ASC
```

`SysMenuRepository.countByIdIn`：

```sql
SELECT COUNT(1)
FROM SYS_MENU
WHERE MENU_ID IN (:ids)
```

`SysRoleMenuRepository.findMenuIdsByRoleId`，JPQL：

```sql
SELECT MENU_ID
FROM SYS_ROLE_MENU
WHERE ROLE_ID = :roleId
```

`SysRoleMenuRepository.deleteByRoleId`，JPQL：

```sql
DELETE FROM SYS_ROLE_MENU
WHERE ROLE_ID = :roleId
```

保存角色菜单时通过 `saveAll` 批量插入：

```sql
INSERT INTO SYS_ROLE_MENU (ROLE_ID, MENU_ID, CREATE_TIME)
VALUES (:roleId, :menuId, :createTime)
```

### 11.10 用户角色关联

`SysUserRoleRepository.findRoleIdsByUserId`，JPQL：

```sql
SELECT ROLE_ID
FROM SYS_USER_ROLE
WHERE USER_ID = :userId
```

`SysUserRoleRepository.findUserIdsByRoleId`，JPQL：

```sql
SELECT USER_ID
FROM SYS_USER_ROLE
WHERE ROLE_ID = :roleId
```

`SysUserRoleRepository.deleteByIdUserId`：

```sql
DELETE FROM SYS_USER_ROLE
WHERE USER_ID = :userId
```

`SysUserRoleRepository.countByIdRoleId`：

```sql
SELECT COUNT(1)
FROM SYS_USER_ROLE
WHERE ROLE_ID = :roleId
```

保存用户角色时通过 `saveAll` 批量插入：

```sql
INSERT INTO SYS_USER_ROLE (USER_ID, ROLE_ID, CREATE_TIME)
VALUES (:userId, :roleId, :createTime)
```

### 11.11 登录日志

`SysLoginLogRepository.save` 插入：

```sql
INSERT INTO SYS_LOGIN_LOG (
  LOG_ID,
  USER_ID,
  USERNAME,
  LOGIN_STATUS,
  LOGIN_MESSAGE,
  LOGIN_IP,
  USER_AGENT,
  LOGIN_TIME
) VALUES (
  SEQ_SYS_LOGIN_LOG.NEXTVAL,
  :userId,
  :username,
  :loginStatus,
  :loginMessage,
  :loginIp,
  :userAgent,
  :loginTime
)
```

## 12. AI 接口 SQL

`AiController.getPriceDeviationInsight` 和 `AiController.getCompanyDiagnosis` 不直接查询数据库。

| 接口 | SQL 使用 |
| --- | --- |
| `POST /api/dashboard/ai/insight/price-deviation` | 无，使用请求体中的图表数据调用 AI |
| `GET /api/dashboard/ai/company-diagnosis` | 无，基于请求参数和缓存调用 AI |

## 13. 附录：Deprecated SalesRepository

`SalesRepository` 已标记 `@Deprecated`，源码注释说明已拆分为 `MetricsRepository`、`PriceAnalysisRepository`、`SalesAnalysisRepository`、`TrendAnalysisRepository`。当前 Controller 链路没有直接依赖 `SalesServiceImpl`，但缓存预热机制会扫描 `@AutoWarmUp` 方法，因此以下 SQL 仍可能被预热任务触发。

| SalesServiceImpl 方法 | SalesRepository 方法 | 当前替代 Repository |
| --- | --- | --- |
| `findSummaryByDate` | `findSummaryByDate` | `MetricsRepository.findSummaryByDate` |
| `findSummaryToToday` | `findSummaryToToday` | `MetricsRepository.findSummaryToToday` |
| `findCountBudget` | `findCountBudget` | `MetricsRepository.findCountBudget` |
| `findAmountBudget` | `findAmountBudget` | 旧 SQL 使用 `ys_xssr_*`，当前 `MetricsRepository.findAmountBudget` 使用 `V_SALES_BUDGET_SUMMARY` |
| `findMonthOrder` | `findMonthOrder` | `MetricsRepository.findMonthOrder` |
| `findYearOrder` | `findYearOrder` | `MetricsRepository.findYearOrder` |
| `findCollection` | `findCollection` | `MetricsRepository.findCollection` |
| `findPriceDiff` | `findPriceDiff` | `PriceAnalysisRepository.findPriceDiff` |
| `findCustomerTransaction` | `findCustomerTransaction` | `PriceAnalysisRepository.findCustomerTransaction`，新方法支持日期区间 |
| `findSaleDetails` | `findCountBudgetDetails`、`findAmountBudgetDetails`、`findSummaryTodayDetails` | `SalesAnalysisRepository` |
| `findSummaryByCompany` | `findSummaryByCompany` | `SalesAnalysisRepository.findSummaryByCompany` |
| `findDetailsByCompany` | `findDetailsByCompany` | `SalesAnalysisRepository.findDetailsByCompany` |
| `findTrendsAll` | `findTrendsAll` | `TrendAnalysisRepository.findTrendsAll` |
| `findTrendsYear` | `findTrendsYear` | `TrendAnalysisRepository.findTrendsYear` |
| `getProductDeepMonth` | `getProductDeepMonth` | `TrendAnalysisRepository.getProductDeepMonth` |
| `getProductDeepYear` | `getProductDeepYear` | `TrendAnalysisRepository.getProductDeepYear` |
| `getProductCustomer` | `getProductCustomer` | `TrendAnalysisRepository.getProductCustomer` |

`SalesRepository.findTrendsToday`、`SalesRepository.findSummaryByDateDetails` 当前没有在 Controller 链路或新拆分 Service 中使用。
