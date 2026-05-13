# 决策支持模块数据库文档

生成日期：2026-05-12

适用模块：`platform-modules/module-decision-support/decision-support-service`

## 1. 数据库连接和持久化配置

| 项目 | 内容 |
| --- | --- |
| 数据源名称 | `spring.datasource.frorcl` |
| 数据库类型 | Oracle |
| 默认 schema | `DY`，配置项为 `app.database.schema` |
| JPA DDL | `spring.jpa.hibernate.ddl-auto=none` |
| Open Session In View | `false` |
| 主要用途 | 读取经营分析视图、维护认证授权表 |

当前模块通过 JPA Repository 使用大量 native SQL。认证授权表由 JPA 实体维护；业务经营数据主要来自 Oracle 视图或已有表，模块内以只读查询为主。

## 2. 认证授权表

### 2.1 通用审计字段

以下表继承或使用通用审计字段：

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| `CREATE_BY` | varchar2(64) | 创建人，默认从当前登录用户取，取不到为 `system` |
| `CREATE_TIME` | timestamp/date | 创建时间，非空 |
| `UPDATE_BY` | varchar2(64) | 更新人 |
| `UPDATE_TIME` | timestamp/date | 更新时间 |

### 2.2 SYS_USER

用途：系统登录用户表。

序列：`SEQ_SYS_USER`

| 字段 | 类型建议 | 主键 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| `USER_ID` | number | 是 | 是 | 用户 ID |
| `USERNAME` | varchar2(50) | 否 | 是 | 登录用户名 |
| `PASSWORD_HASH` | varchar2(255) | 否 | 是 | BCrypt 密码摘要 |
| `NICKNAME` | varchar2(100) | 否 | 否 | 昵称 |
| `REAL_NAME` | varchar2(100) | 否 | 否 | 真实姓名 |
| `EMAIL` | varchar2(100) | 否 | 否 | 邮箱 |
| `MOBILE` | varchar2(30) | 否 | 否 | 手机号 |
| `DING_USER_ID` | varchar2(64) | 否 | 否 | 钉钉用户 ID |
| `DING_UNION_ID` | varchar2(64) | 否 | 否 | 钉钉 unionId |
| `STATUS` | number | 否 | 是 | `1` 启用，`0` 禁用 |
| `DEL_FLAG` | varchar2(1) | 否 | 是 | 逻辑删除标识，`0` 正常 |
| `LAST_LOGIN_TIME` | timestamp/date | 否 | 否 | 最后登录时间 |
| `LAST_LOGIN_IP` | varchar2(64) | 否 | 否 | 最后登录 IP |
| `CREATE_BY` | varchar2(64) | 否 | 否 | 创建人 |
| `CREATE_TIME` | timestamp/date | 否 | 是 | 创建时间 |
| `UPDATE_BY` | varchar2(64) | 否 | 否 | 更新人 |
| `UPDATE_TIME` | timestamp/date | 否 | 否 | 更新时间 |

建议约束和索引：

| 类型 | 字段 | 说明 |
| --- | --- | --- |
| unique | `USERNAME` | 用户名唯一 |
| index | `DING_USER_ID` | 钉钉登录查询 |
| index | `DING_UNION_ID` | 钉钉用户绑定查询 |
| index | `STATUS`, `DEL_FLAG` | 用户列表和登录过滤 |

### 2.3 SYS_ROLE

用途：系统角色表。

序列：`SEQ_SYS_ROLE`

| 字段 | 类型建议 | 主键 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| `ROLE_ID` | number | 是 | 是 | 角色 ID |
| `ROLE_NAME` | varchar2(60) | 否 | 是 | 角色名称 |
| `ROLE_KEY` | varchar2(100) | 否 | 是 | 角色标识，例如 `system:admin` |
| `ROLE_SORT` | number | 否 | 是 | 排序 |
| `STATUS` | number | 否 | 是 | `1` 启用，`0` 禁用 |
| `REMARK` | varchar2(500) | 否 | 否 | 备注 |
| `CREATE_BY` | varchar2(64) | 否 | 否 | 创建人 |
| `CREATE_TIME` | timestamp/date | 否 | 是 | 创建时间 |
| `UPDATE_BY` | varchar2(64) | 否 | 否 | 更新人 |
| `UPDATE_TIME` | timestamp/date | 否 | 否 | 更新时间 |

建议约束和索引：

| 类型 | 字段 | 说明 |
| --- | --- | --- |
| unique | `ROLE_KEY` | 角色标识唯一 |
| index | `STATUS`, `ROLE_SORT` | 角色列表排序和过滤 |

### 2.4 SYS_MENU

用途：菜单和权限点表。接口鉴权依赖 `PERMS` 字段。

序列：`SEQ_SYS_MENU`

| 字段 | 类型建议 | 主键 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| `MENU_ID` | number | 是 | 是 | 菜单 ID |
| `PARENT_ID` | number | 否 | 是 | 父菜单 ID，根节点为 `0` |
| `MENU_NAME` | varchar2(100) | 否 | 是 | 菜单/权限名称 |
| `MENU_TYPE` | varchar2(1) | 否 | 是 | `M` 目录，`C` 菜单页面，`B` 按钮/权限 |
| `PATH` | varchar2(200) | 否 | 否 | 前端路由路径 |
| `COMPONENT` | varchar2(200) | 否 | 否 | 前端组件路径 |
| `PERMS` | varchar2(200) | 否 | 否 | 权限标识，例如 `decision:metrics:view` |
| `ICON` | varchar2(100) | 否 | 否 | 图标 |
| `ORDER_NUM` | number | 否 | 是 | 排序 |
| `STATUS` | number | 否 | 是 | `1` 启用，`0` 禁用 |
| `REMARK` | varchar2(500) | 否 | 否 | 备注 |
| `CREATE_BY` | varchar2(64) | 否 | 否 | 创建人 |
| `CREATE_TIME` | timestamp/date | 否 | 是 | 创建时间 |
| `UPDATE_BY` | varchar2(64) | 否 | 否 | 更新人 |
| `UPDATE_TIME` | timestamp/date | 否 | 否 | 更新时间 |

建议约束和索引：

| 类型 | 字段 | 说明 |
| --- | --- | --- |
| unique | `PERMS`，允许空值 | 权限点不重复 |
| index | `PARENT_ID`, `ORDER_NUM` | 菜单树查询 |
| index | `STATUS` | 启用状态过滤 |

### 2.5 SYS_USER_ROLE

用途：用户和角色关联表。

| 字段 | 类型建议 | 主键 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| `USER_ID` | number | 是 | 是 | 用户 ID |
| `ROLE_ID` | number | 是 | 是 | 角色 ID |
| `CREATE_TIME` | timestamp/date | 否 | 否 | 创建时间 |

主键：`USER_ID` + `ROLE_ID`

建议索引：`ROLE_ID`，用于反查角色下用户。

### 2.6 SYS_ROLE_MENU

用途：角色和菜单权限关联表。

| 字段 | 类型建议 | 主键 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| `ROLE_ID` | number | 是 | 是 | 角色 ID |
| `MENU_ID` | number | 是 | 是 | 菜单 ID |
| `CREATE_TIME` | timestamp/date | 否 | 否 | 创建时间 |

主键：`ROLE_ID` + `MENU_ID`

建议索引：`MENU_ID`，用于反查权限分配。

### 2.7 SYS_LOGIN_LOG

用途：登录日志表。

序列：`SEQ_SYS_LOGIN_LOG`

| 字段 | 类型建议 | 主键 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| `LOG_ID` | number | 是 | 是 | 日志 ID |
| `USER_ID` | number | 否 | 否 | 用户 ID |
| `USERNAME` | varchar2(50) | 否 | 否 | 用户名 |
| `LOGIN_STATUS` | number | 否 | 是 | 登录状态 |
| `LOGIN_MESSAGE` | varchar2(500) | 否 | 否 | 登录结果描述 |
| `LOGIN_IP` | varchar2(64) | 否 | 否 | 登录 IP |
| `USER_AGENT` | varchar2(1000) | 否 | 否 | 浏览器 UA |
| `LOGIN_TIME` | timestamp/date | 否 | 是 | 登录时间 |

建议索引：`USERNAME`、`USER_ID`、`LOGIN_TIME`。

## 3. 决策支持权限初始化

脚本位置：`docs/decision-permissions-oracle-init.sql`

脚本作用：

1. 初始化 `SYS_MENU` 中的“决策支持”根目录。
2. 初始化 `decision:*` 权限点。
3. 将上述权限授权给 `ROLE_KEY = 'system:admin'` 的角色。
4. 脚本使用 `MERGE` 和 `NOT EXISTS`，可重复执行。

初始化的权限点：

| 菜单名称 | 权限标识 | 类型 | 前端路径 |
| --- | --- | --- | --- |
| 销售指标大盘 | `decision:metrics:view` | `C` | `/` |
| 价格分析 | `decision:price-analysis:view` | `B` | 空 |
| 销售分析 | `decision:sales-analysis:view` | `C` | `/details/sales` |
| 趋势分析 | `decision:trend-analysis:view` | `B` | 空 |
| 回款分析 | `decision:collection-analysis:view` | `C` | `/details/collection` |
| 三费监控 | `decision:expense:view` | `C` | `/expense-monitor` |
| 明细数据查询 | `decision:all-details:view` | `C` | `/all-details` |
| AI价格洞察 | `decision:ai:insight` | `B` | 空 |
| AI公司诊断 | `decision:ai:diagnosis` | `B` | 空 |

## 4. 经营分析数据对象

以下对象主要为已有业务表或视图，模块内不维护表结构，只通过 native SQL 读取。

### 4.1 v_sales_detail_all

用途：销售明细核心视图，被核心指标、价格分析、销售分析、趋势分析、明细查询使用。

| 字段 | 说明 | 使用场景 |
| --- | --- | --- |
| `销售主产` | 是否销售主产，通常过滤为 `1` | 价格、销售、趋势 |
| `物料` | 物料编码 | 兼容投影 |
| `物料描述` | 物料名称 | 明细查询 |
| `物料组` | 产品/物料组编码 | 价格、趋势、产品深度 |
| `物料组描述` | 产品/物料组名称 | 价格、销售、趋势 |
| `渠道` | 渠道编码，`10` 国内，`20` 国外 | 区域拆分 |
| `销量` | 销售量 | 指标、分析 |
| `金额` | 销售金额，代码按“万元”展示口径处理 | 指标、分析 |
| `单价` | 单价 | 明细、订单发货明细 |
| `客户名称` | 客户名称 | 客户交易、产品客户排行 |
| `过账日期` | 业务日期，字符串格式 `yyyy-MM-dd` | 所有销售类日期过滤 |
| `工厂` | 工厂编码，`1201` 高分子，`1301` 氟硅，`3001` 绿冷，`1400` 有机硅 | 公司名称映射 |
| `公司编码` | 公司编码，`1200`、`1300`、`3000`、`1400` | 公司过滤 |

常用过滤条件：

| 条件 | 含义 |
| --- | --- |
| `物料组描述 <> '无价值物料'` | 排除无价值物料 |
| `销售主产 = '1'` | 只统计销售主产 |
| `销量 <> 0` | 排除零销量，避免单价计算异常 |
| `过账日期 BETWEEN :startDate AND :endDate` | 按业务日期区间过滤 |

建议索引或物化视图维度：`过账日期`、`公司编码`、`工厂`、`物料组`、`渠道`、`销售主产`。

### 4.2 V_SALES_BUDGET_SUMMARY

用途：销售预算汇总视图。

| 字段 | 说明 | 使用场景 |
| --- | --- | --- |
| `月份` | 月份，格式 `yyyy-MM` | 核心指标、销售公司列表 |
| `公司编码` | 公司编码 | 公司预算 |
| `销量预算` | 月销量预算 | 销量完成率 |
| `销售额预算` | 月销售额预算 | 销售额完成率 |

建议索引：`月份`、`公司编码`。

### 4.3 回款来源视图

#### 4.3.1 v_lvleng_huikuan

用途：绿冷回款统计。

| 字段 | 说明 |
| --- | --- |
| `回款` | 回款金额 |
| `渠道` | 渠道，代码排除 `关联` |
| `年度` | 年度 |
| `期间` | 月份 |

#### 4.3.2 v_gfz_huikuan

用途：高分子回款统计。

| 字段 | 说明 |
| --- | --- |
| `回款` | 回款金额 |
| `渠道` | 渠道，代码排除 `关联` |
| `财年` | 年度 |
| `过账期间` | 月份 |

#### 4.3.3 v_fg_huikuan

用途：氟硅回款统计。

| 字段 | 说明 |
| --- | --- |
| `回款` | 回款金额 |
| `渠道` | 渠道，代码排除 `关联` |
| `财年` | 年度 |
| `过账期间` | 月份 |

#### 4.3.4 V_YJG_HUIKUAN

用途：有机硅回款统计。

| 字段 | 说明 |
| --- | --- |
| `回款` | 回款金额 |
| `渠道` | 渠道，代码排除 `关联` |
| `时间戳` | 时间字段，按 `yyyy-MM` 过滤 |

### 4.4 V_HUIKUAN_ALL

用途：回款分析公司列表。

| 字段 | 说明 |
| --- | --- |
| `公司编码` | `1200` 高分子，`1300` 氟硅，`3000` 绿冷，`1400` 有机硅 |
| `回款` | 回款金额 |
| `SN` | 排序号 |
| `会计期间` | 期间，格式 `yyyy-MM` |

建议索引：`会计期间`、`公司编码`。

### 4.5 SO_PLANACCOUNT

用途：回款计划/目标。

| 字段 | 说明 |
| --- | --- |
| `COMPANY` | 公司名称 |
| `PLANACCOUNTS` | 计划回款额 |
| `CHECKYEAR` | 年度 |
| `CHECKMONTH` | 月份 |

代码通过 `CHECKYEAR || '-' || CHECKMONTH` 和目标月份匹配。

### 4.6 so_order

用途：普通订单统计。

| 字段 | 说明 |
| --- | --- |
| `创建日期` | 订单创建日期 |
| `交货状态` | 交货状态，`完全处理` 视为关闭 |
| `工厂` | 工厂编码，订单指标过滤 `1301`、`1201`、`3001` |

建议索引：`创建日期`、`工厂`、`交货状态`。

### 4.7 v_yjg_order_qty

用途：有机硅订单数量/状态统计。

| 字段 | 说明 |
| --- | --- |
| `时间戳` | 统计时间 |
| `单据状态` | 订单状态，`关闭` 视为关闭 |
| `公司编码` | 公司编码，当前过滤 `1400` |

建议索引：`公司编码`、`时间戳`、`单据状态`。

### 4.8 v_order_detail

用途：销售分析中的订单明细主表。

| 字段 | 说明 |
| --- | --- |
| `order_date` | 订单日期 |
| `order_no` | 订单号 |
| `material_group` | 物料组 |
| `material_desc` | 物料描述 |
| `delivery_status` | 交货状态 |
| `sales_org` | 销售组织 |
| `office` | 办事处 |
| `salesperson` | 销售员 |
| `customer` | 客户 |
| `channel` | 渠道，代码排除 `公司间` |
| `order_num` | 订单数量 |
| `order_amount` | 订单金额 |

建议索引：`order_date`、`order_no`、`sales_org`、`channel`。

### 4.9 dwm_v_jt_dingdan_fahuo

用途：订单发货明细，和 `v_order_detail.order_no` 关联。

| 字段 | 说明 |
| --- | --- |
| `订单号` | 订单号 |
| `物料` | 物料 |
| `物料描述` | 物料描述 |
| `销量` | 发货量 |
| `金额` | 发货金额 |
| `单价` | 单价 |
| `办事处描述` | 办事处 |
| `客户名称` | 客户名称 |
| `日期` | 发货日期 |

建议索引：`订单号`、`日期`。

### 4.10 v_dwm_jt_fapiao_mingxi_std

用途：发票明细标准视图，用于年度趋势、产品年度深度和客户排行。

| 字段 | 说明 |
| --- | --- |
| `公司编码` | 公司编码 |
| `渠道` | 渠道。部分 SQL 按 `国内` / `国外` 转为 `10` / `20` |
| `物料组` | 产品编码 |
| `物料组描述` | 产品名称 |
| `日期` | 日期字符串，通常取 `yyyy-MM-dd` 或 `yyyy-MM` |
| `开票量` | 开票数量 |
| `开票金额` | 开票金额 |
| `客户名称` | 客户名称 |

建议索引：`日期`、`公司编码`、`物料组`、`渠道`。

### 4.11 dwm_v_jt_sanfei

用途：三费月度汇总视图。

| 字段 | 说明 |
| --- | --- |
| `月份` | 月份，格式 `yyyy-MM` |
| `项目` | 费用项目，代码通过 `LIKE '%销售费用%'`、`LIKE '%管理费用%'`、`LIKE '%财务费用%'` 分类 |
| `期末余额` | 费用金额 |
| `公司名称` | 公司名称 |

使用接口：

| 接口 | 用途 |
| --- | --- |
| `/api/expense/overview` | 本月和上月三费总览 |
| `/api/expense/company-detail` | 公司三费分页明细 |
| `/api/expense/company-comparison` | 公司费用对比 |
| `/api/expense/structure` | 费用结构 |
| `/api/expense/trend` | 12 个月趋势 |
| `/api/expense/growth` | 同比、环比 |

建议索引：`月份`、`公司名称`、`项目`。

### 4.12 dwm_v_jt_sanfei_mingxi

用途：三费每日明细。

| 字段 | 说明 |
| --- | --- |
| `过账日期` | 费用日期 |
| `公司名称` | 公司名称 |
| `费用类型` | 费用类型 |
| `本币金额` | 金额，代码中乘以 `10000` |
| `行项目文本` | 摘要文本 |

当前 `ExpenseRepository.getDailyDetail` 和 `ExpenseService.getDailyDetail` 已存在，但 `ExpenseController` 未暴露接口。

建议索引：`过账日期`、`公司名称`、`费用类型`。

### 4.13 YS_FEIYONG_JT

用途：三费预算表。

| 字段 | 说明 |
| --- | --- |
| `公司` | 公司名称 |
| `月份` | 月份，格式 `yyyy-MM` |
| `销售费用` | 销售费用预算 |
| `管理费用` | 管理费用预算 |
| `财务费用` | 财务费用预算 |

使用接口：`/api/expense/budget-execution`

当 `dimension=year` 时查询从当年 1 月到当前月；否则只查询当前月。

建议索引：`月份`、`公司`。

### 4.14 V_LVLENG_RIXIAOSHOU

用途：JPA Repository 的实体映射基础对象。当前多数 Repository 实际 native SQL 查询其他视图，此实体主要用于满足 Spring Data Repository 泛型要求。

| 字段 | 说明 |
| --- | --- |
| `id` | 实体主键字段 |
| 其他字段 | 以实体 `VLvlengRixiaoshou` 为准 |

## 5. 主要接口和数据对象关系

| 接口组 | Repository | 主要数据对象 |
| --- | --- | --- |
| 核心指标 `/api/metrics` | `MetricsRepository` | `v_sales_detail_all`、`V_SALES_BUDGET_SUMMARY`、各回款视图 |
| 订单指标 `/api/metrics/orders` | `MetricsRepository` | `so_order`、`v_yjg_order_qty` |
| 价格分析 `/api/price-analysis/**` | `PriceAnalysisRepository` | `v_sales_detail_all` |
| 销售分析 `/api/sales-analysis/companies*` | `SalesAnalysisRepository` | `v_sales_detail_all`、`V_SALES_BUDGET_SUMMARY` |
| 订单详情 `/api/sales-analysis/orders/company-detail` | `OrderRepository` | `v_order_detail`、`dwm_v_jt_dingdan_fahuo` |
| 产品深度 `/api/sales-analysis/product-deep` | `TrendAnalysisRepository` | `v_sales_detail_all`、`v_dwm_jt_fapiao_mingxi_std` |
| 趋势分析 `/api/trend-analysis/**` | `TrendAnalysisRepository` | `v_sales_detail_all`、`v_dwm_jt_fapiao_mingxi_std` |
| 回款分析 `/api/collection-analysis/companies` | `CollectionRepository` | `V_HUIKUAN_ALL`、`SO_PLANACCOUNT` |
| 三费监控 `/api/expense/**` | `ExpenseRepository` | `dwm_v_jt_sanfei`、`dwm_v_jt_sanfei_mingxi`、`YS_FEIYONG_JT` |
| 明细查询 `/api/all_details/sale_details` | `AllDetailsRepository` | `v_sales_detail_all` |
| 认证和权限 `/api/v1/auth/**`、`/api/v1/system/roles/**` | system repositories | `SYS_USER`、`SYS_ROLE`、`SYS_MENU`、关联表、`SYS_LOGIN_LOG` |

## 6. 数据口径说明

### 6.1 销售日口径

多个接口传入 `date` 后使用 `date - 1` 作为实际业务日期，例如：

| 接口 | 口径 |
| --- | --- |
| `/api/metrics` | 日指标查 `date - 1`，月累计从 `date - 1` 所在月月初到 `date - 1` |
| `/api/metrics/orders` | 本月订单按 `date - 1` 所在月 |
| `/api/sales-analysis/companies` | 公司销售累计到 `date - 1` |
| `/api/sales-analysis/companies/detail` | 查询 `date - 1` 当天产品明细 |
| `/api/sales-analysis/product-deep?type=month` | 本月从月初到 `date - 1` |

### 6.2 金额和价格口径

| 位置 | 口径 |
| --- | --- |
| 销售金额 `金额` | 多数接口展示为万元口径 |
| 价格计算 | 常见公式为 `金额 / 销量 * 10000` |
| 三费金额 | DTO 单位为 `万`，部分 SQL/Service 对金额做转换 |

### 6.3 公司映射差异

销售明细中常见两套编码：

| 来源字段 | 高分子 | 氟硅 | 绿冷 | 有机硅 |
| --- | --- | --- | --- | --- |
| `工厂` | `1201` | `1301` | `3001` | `1400` |
| `公司编码` | `1200` | `1300` | `3000` | `1400` |

Controller 层接收公司名称时通常按 `CompanyCodeConstant` 映射为公司编码：

| 公司名称 | 公司编码 |
| --- | --- |
| 高分子 | `1200` |
| 氟硅 | `1300` |
| 绿冷 | `3000` |
| 有机硅 | `1400` |

## 7. 缓存影响

部分 Service 通过 `CaffeineCacheService` 缓存当天数据，缓存类型多为 `CacheType.TODAY_DATA`。

| 业务 | 缓存 key 示例 |
| --- | --- |
| 核心指标 | `summary:<date>`、`todaySum:<date>`、`countBudget:<date>` |
| 订单指标 | `monthOrder:<date>`、`yearOrder:<date>` |
| 价格偏差 | `priceDiff:<endDate>` |
| 趋势 | `trendsAll:<endDate>`、`trendsYear:<endDate>:<productCode>_<region>` |
| 产品深度 | `productDeep:<companyName>:<productCode>:<type>:<date>` |

如果底层视图数据刷新后接口仍返回旧值，需要检查缓存过期策略或重启服务。

## 8. 需要重点核对的数据风险

1. 多个 SQL 对 `过账日期`、`日期` 使用字符串比较或 `TO_DATE`，底层字段格式必须稳定为 `yyyy-MM-dd`。
2. `/api/price-analysis/deviations/details` 使用服务器当前日期，不跟随前端传入日期。
3. `/api/trend-analysis/monthly` 和 `/api/trend-analysis/yearly` 当前忽略请求 `date`，使用服务器当前日期。
4. `ExpenseService.getDailyDetail` 已有 Repository SQL，但 Controller 未暴露。
5. 三费公司对比 DTO 有 `total`、`yoy` 字段，但当前服务可能不赋值。
6. 产品深度趋势中 `intlAmount` 当前代码累加逻辑需要复核：国际金额分支使用了销量累加到 `intlAmount` 的语句，可能影响展示。

## 9. 建议的数据库检查 SQL

检查权限是否已写入：

```sql
SELECT MENU_ID, MENU_NAME, MENU_TYPE, PATH, PERMS, STATUS
  FROM SYS_MENU
 WHERE PATH = '/decision-support'
    OR PERMS LIKE 'decision:%'
 ORDER BY PARENT_ID, ORDER_NUM, MENU_ID;
```

检查管理员是否已授权：

```sql
SELECT R.ROLE_KEY, M.MENU_NAME, M.PERMS
  FROM SYS_ROLE_MENU RM
  JOIN SYS_ROLE R ON R.ROLE_ID = RM.ROLE_ID
  JOIN SYS_MENU M ON M.MENU_ID = RM.MENU_ID
 WHERE R.ROLE_KEY = 'system:admin'
   AND (M.PERMS LIKE 'decision:%' OR M.PATH = '/decision-support')
 ORDER BY M.ORDER_NUM, M.MENU_ID;
```

检查用户登录后可获得的权限：

```sql
SELECT DISTINCT M.PERMS
  FROM SYS_USER U
  JOIN SYS_USER_ROLE UR ON UR.USER_ID = U.USER_ID
  JOIN SYS_ROLE R ON R.ROLE_ID = UR.ROLE_ID
  JOIN SYS_ROLE_MENU RM ON RM.ROLE_ID = R.ROLE_ID
  JOIN SYS_MENU M ON M.MENU_ID = RM.MENU_ID
 WHERE U.USERNAME = :username
   AND U.STATUS = 1
   AND U.DEL_FLAG = '0'
   AND R.STATUS = 1
   AND M.STATUS = 1
   AND M.PERMS IS NOT NULL
 ORDER BY M.PERMS;
```

检查销售核心视图日期格式：

```sql
SELECT 过账日期, COUNT(1)
  FROM v_sales_detail_all
 WHERE ROWNUM <= 100
 GROUP BY 过账日期
 ORDER BY 过账日期 DESC;
```
