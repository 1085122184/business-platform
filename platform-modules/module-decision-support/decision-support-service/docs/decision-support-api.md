# 决策支持模块接口文档

适用模块：`platform-modules/module-decision-support/decision-support-service`

## 1. 基础信息

| 项目 | 内容 |
| --- | --- |
| 服务端口 | `18080` |
| Context Path | `/` |
| OpenAPI JSON | `/api-docs` |
| Swagger UI | `/swagger-ui.html` |
| 鉴权方式 | JWT Bearer Token |
| 统一响应 | `Result<T>`，字段为 `code`、`message`、`data`、`timestamp` |
| 成功码 | `200` |
| 常见错误码 | `400` 参数错误，`401` 未登录，`403` 无权限，`500` 服务异常 |

除登录、钉钉登录、登录票据消费、Swagger、OPTIONS 外，其余接口都需要请求头：

```http
Authorization: Bearer <accessToken>
```

统一响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1710000000000
}
```

## 2. 权限点

| 权限标识 | 覆盖接口 |
| --- | --- |
| `decision:metrics:view` | `/api/metrics`、`/api/metrics/orders` |
| `decision:price-analysis:view` | `/api/price-analysis/**` |
| `decision:sales-analysis:view` | `/api/sales-analysis/**` |
| `decision:trend-analysis:view` | `/api/trend-analysis/**` |
| `decision:collection-analysis:view` | `/api/collection-analysis/**` |
| `decision:expense:view` | `/api/expense/**` |
| `decision:all-details:view` | `/api/all_details/**` |
| `decision:ai:insight` | `/api/dashboard/ai/insight/price-deviation` |
| `decision:ai:diagnosis` | `/api/dashboard/ai/company-diagnosis` |
| `system:role:list` | 角色、用户、菜单查询 |
| `system:role:add` | 新增角色、新增用户 |
| `system:role:edit` | 修改角色、修改用户 |
| `system:role:remove` | 删除角色、删除用户 |
| `system:role:grant` | 角色菜单授权 |

拥有 `*:*:*` 的用户会跳过具体权限点校验。

## 3. 公共枚举和约定

### 3.1 日期

| 参数类型 | 格式 |
| --- | --- |
| `LocalDate` | `yyyy-MM-dd` |
| 三费增长 `date` | 至少包含 `yyyy-MM`，代码取 `date.substring(0, 7)` |

### 3.2 公司名称和公司编码

| 公司名称 | 公司编码 |
| --- | --- |
| 绿冷 | `3000` |
| 有机硅 | `1400` |
| 氟硅 | `1300` |
| 高分子 | `1200` |

### 3.3 区域

| 展示值 | 源码 |
| --- | --- |
| 国内 | `10` |
| 国外 | `20` |

### 3.4 分析类型

| 参数 | 含义 |
| --- | --- |
| `volume` | 销量 |
| `amount` | 销售额 |
| `month` | 月度 |
| `year` | 年度 |

## 4. 认证接口

### 4.1 用户名密码登录

| 项目 | 内容 |
| --- | --- |
| 方法 | `POST` |
| 路径 | `/api/v1/auth/login` |
| 是否公开 | 是 |

请求体：

| 字段 | 类型 | 必填 | 规则 | 说明 |
| --- | --- | --- | --- | --- |
| `username` | string | 是 | 2-50 位 | 用户名 |
| `password` | string | 是 | 6-50 位 | 明文密码，后端使用 BCrypt 校验 |

响应 `data`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `accessToken` | string | 访问令牌 |
| `refreshToken` | string | 刷新令牌 |
| `tokenType` | string | 固定为 `Bearer` |
| `expiresIn` | number | 访问令牌有效秒数，默认 7200 |
| `userInfo` | object | 当前用户信息 |
| `roles` | string[] | 角色 key 列表 |
| `permissions` | string[] | 权限标识列表 |

`userInfo` 字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `userId` | number | 用户 ID |
| `username` | string | 用户名 |
| `nickname` | string | 昵称 |
| `status` | number | 状态，`1` 启用，`0` 禁用 |

### 4.2 钉钉免密登录

| 项目 | 内容 |
| --- | --- |
| 方法 | `POST` |
| 路径 | `/api/v1/auth/dingtalk/login` |
| 是否公开 | 是 |

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `authCode` | string | 是 | 钉钉授权码 |

响应同用户名密码登录。

### 4.3 PC 桥接登录

| 项目 | 内容 |
| --- | --- |
| 方法 | `POST` |
| 路径 | `/api/v1/auth/dingtalk/bridge-login` |
| 是否公开 | 是 |

Query 参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `redirect` | string | 否 | 登录后跳转地址 |

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `authCode` | string | 是 | 钉钉授权码 |

响应 `data`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `externalUrl` | string | 带一次性票据的外部跳转地址 |
| `expiresIn` | number | 票据有效秒数，当前为 120 |

### 4.4 消费登录票据

| 项目 | 内容 |
| --- | --- |
| 方法 | `POST` |
| 路径 | `/api/v1/auth/login-ticket/consume` |
| 是否公开 | 是 |

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `ticket` | string | 是 | 一次性登录票据 |

响应同用户名密码登录。

### 4.5 退出登录

| 项目 | 内容 |
| --- | --- |
| 方法 | `POST` |
| 路径 | `/api/v1/auth/logout` |
| 鉴权 | 登录态 |

响应 `data`：`boolean`

### 4.6 当前登录用户

| 项目 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/v1/auth/profile` |
| 鉴权 | 登录态 |

响应 `data`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `userInfo` | object | 当前用户信息 |
| `roles` | string[] | 角色 key 列表 |
| `permissions` | string[] | 权限标识列表 |

## 5. 系统角色和用户接口

### 5.1 分页查询角色

| 项目 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/v1/system/roles` |
| 权限 | `system:role:list` |

Query 参数：

| 字段 | 类型 | 必填 | 规则 | 说明 |
| --- | --- | --- | --- | --- |
| `pageNum` | number | 是 | >= 1 | 页码 |
| `pageSize` | number | 是 | >= 1 | 页大小 |
| `roleName` | string | 否 | - | 角色名称模糊查询 |
| `roleKey` | string | 否 | - | 角色 key 模糊查询 |
| `status` | number | 否 | `0`/`1` | 状态 |

响应 `data`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `list` | RoleResponse[] | 角色列表 |
| `rows` | RoleResponse[] | 兼容字段，同 `list` |
| `total` | number | 总数 |
| `pageNum` | number | 页码 |
| `pageSize` | number | 页大小 |

`RoleResponse`：`id`、`roleName`、`roleKey`、`roleSort`、`status`、`remark`、`createTime`、`updateTime`。

### 5.2 新增角色

| 项目 | 内容 |
| --- | --- |
| 方法 | `POST` |
| 路径 | `/api/v1/system/roles` |
| 权限 | `system:role:add` |

请求体：

| 字段 | 类型 | 必填 | 规则 | 说明 |
| --- | --- | --- | --- | --- |
| `roleName` | string | 是 | 2-30 位 | 角色名称 |
| `roleKey` | string | 是 | 2-100 位 | 角色标识 |
| `roleSort` | number | 是 | 0-9999 | 排序 |
| `status` | number | 是 | `0`/`1` | 状态 |
| `remark` | string | 否 | <= 500 位 | 备注 |

响应 `data`：新角色 ID。

### 5.3 修改角色

| 项目 | 内容 |
| --- | --- |
| 方法 | `PUT` |
| 路径 | `/api/v1/system/roles` |
| 权限 | `system:role:edit` |

请求体在新增角色基础上增加：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | number | 是 | 角色 ID |

响应 `data`：`boolean`

### 5.4 删除角色

| 项目 | 内容 |
| --- | --- |
| 方法 | `DELETE` |
| 路径 | `/api/v1/system/roles/{id}` |
| 权限 | `system:role:remove` |

响应 `data`：`boolean`

### 5.5 新增用户

| 项目 | 内容 |
| --- | --- |
| 方法 | `POST` |
| 路径 | `/api/v1/system/roles/users` |
| 权限 | `system:role:add` |

请求体：

| 字段 | 类型 | 必填 | 规则 | 说明 |
| --- | --- | --- | --- | --- |
| `username` | string | 是 | 2-50 位 | 用户名 |
| `password` | string | 是 | 6-64 位 | 初始密码 |
| `nickname` | string | 否 | <= 100 位 | 昵称 |
| `realName` | string | 否 | <= 100 位 | 真实姓名 |
| `email` | string | 否 | 邮箱格式，<= 100 位 | 邮箱 |
| `mobile` | string | 否 | <= 30 位 | 手机号 |
| `status` | number | 是 | `0`/`1` | 状态 |
| `roleIds` | number[] | 是 | 非空 | 角色 ID 列表 |

响应 `data`：新用户 ID。

### 5.6 分页查询用户

| 项目 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/v1/system/roles/users` |
| 权限 | `system:role:list` |

Query 参数：`pageNum`、`pageSize` 必填且 >= 1；`username`、`mobile`、`status` 可选。

响应 `data`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `list` | UserResponse[] | 用户列表 |
| `rows` | UserResponse[] | 兼容字段，同 `list` |
| `total` | number | 总数 |
| `pageNum` | number | 页码 |
| `pageSize` | number | 页大小 |

`UserResponse`：`id`、`username`、`nickname`、`realName`、`email`、`mobile`、`dingUserId`、`status`、`roleIds`、`roleNames`、`createTime`、`updateTime`。

### 5.7 修改用户

| 项目 | 内容 |
| --- | --- |
| 方法 | `PUT` |
| 路径 | `/api/v1/system/roles/users` |
| 权限 | `system:role:edit` |

请求体：

| 字段 | 类型 | 必填 | 规则 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | number | 是 | - | 用户 ID |
| `password` | string | 否 | 6-64 位 | 新密码，不传则不改 |
| `nickname` | string | 否 | <= 100 位 | 昵称 |
| `realName` | string | 否 | <= 100 位 | 真实姓名 |
| `email` | string | 否 | 邮箱格式，<= 100 位 | 邮箱 |
| `mobile` | string | 否 | <= 30 位 | 手机号 |
| `status` | number | 是 | `0`/`1` | 状态 |
| `roleIds` | number[] | 是 | 非空 | 角色 ID 列表 |

响应 `data`：`boolean`

### 5.8 删除用户

| 项目 | 内容 |
| --- | --- |
| 方法 | `DELETE` |
| 路径 | `/api/v1/system/roles/users/{id}` |
| 权限 | `system:role:remove` |

响应 `data`：`boolean`

### 5.9 菜单树和角色授权

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| `GET` | `/api/v1/system/roles/menu-tree` | `system:role:list` | 获取菜单树 |
| `GET` | `/api/v1/system/roles/{roleId}/menus` | `system:role:grant` | 获取角色已分配菜单 ID |
| `PUT` | `/api/v1/system/roles/{roleId}/menus` | `system:role:grant` | 保存角色菜单 ID 列表 |

菜单树节点字段：`id`、`menuName`、`parentId`、`children`。

保存角色菜单请求体：

```json
[1, 2, 3]
```

## 6. 核心指标接口

### 6.1 销售和回款指标

| 项目 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/metrics` |
| 权限 | `decision:metrics:view` |

Query 参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `date` | date | 建议必填 | 代码会直接使用 `date.format`，不传会有空指针风险 |

业务口径：查询 `date - 1` 的销售日数据，月累计区间为 `date - 1` 所在月月初到 `date - 1`。

响应 `data`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `salesVolume` | RawSalesMetric | 销量指标 |
| `salesAmount` | RawSalesMetric | 销售额指标 |
| `collection` | RawCollection | 回款指标 |

`RawSalesMetric`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `metricName` | string | 指标名 |
| `displayValue` | string | 展示值，例如 `123 吨`、`456 万元` |
| `type` | string | `volume` 或 `amount` |
| `budgetRate` | number | 月累计完成率，小数值 |
| `gapValue` | number | 当前代码返回月累计实际值 |
| `monthGoal` | number | 月预算目标 |

`RawCollection`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `collectionAmount` | string | 回款展示值 |
| `collectionRate` | number | 回款 / 月累计销售额，小数值 |
| `gapValue` | number | 月累计销售额 - 回款 |
| `monthGoal` | number | 月累计销售额 |

### 6.2 订单指标

| 项目 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/metrics/orders` |
| 权限 | `decision:metrics:view` |

Query 参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `date` | date | 建议必填 | 代码会直接使用 `date.minusDays` |

响应 `data`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `monthOrders` | RawOrder | 本月未关订单 |
| `yearOrders` | RawOrder | 本年未关订单 |

`RawOrder`：`orderTitle`、`orderCount`、`orderRate`、`barColor`。

## 7. 价格分析接口

### 7.1 价格偏差列表

| 项目 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/price-analysis/deviations` |
| 权限 | `decision:price-analysis:view` |

Query 参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `date` | date | 建议必填 | 查询 `date - 7` 到 `date - 1`，不传会有空指针风险 |

响应 `data`：`RawPriceDeviation[]`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `productName` | string | 物料组描述 |
| `productCode` | string | 物料组 |
| `region` | string | 国内 / 国外 |
| `avgPrice7d` | number | 近 7 日均价 |
| `todayPrice` | number | `date - 1` 当日单价 |
| `deviationAmt` | number | 当日单价 - 近 7 日均价 |
| `deviationPct` | number | 偏差百分比，例如 `-11.11` |

### 7.2 价格偏差客户详情

| 项目 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/price-analysis/deviations/details` |
| 权限 | `decision:price-analysis:view` |

Query 参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `code` | string | 是 | 物料组编码 |
| `region` | string | 是 | `国内` 或 `国外`，其他值默认按国内 |
| `type` | string | 是 | `7days` 表示近 7 天，否则查询昨日 |

响应 `data`：`CustomerTransactionProjection[]`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `customer` | string | 客户名称 |
| `volume` | number | 提货量 |
| `price` | number | 成交单价 |

注意：该接口当前不接收 `date`，Service 使用服务器当前日期的昨日作为结束日期。

## 8. 趋势分析接口

### 8.1 月度量价趋势

| 项目 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/trend-analysis/monthly` |
| 权限 | `decision:trend-analysis:view` |

Query 参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `date` | string | 是 | Controller 要求必传；当前 Service 实际使用服务器当前日期计算近 30 天 |

响应 `data`：`SalesTrendProductDTO[]`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `productCode` | string | 物料组 |
| `product` | string | 产品名称 |
| `region` | string | 国内 / 国外 |
| `latestDate` | string | 最新业务日期 |
| `latestVolume` | number | 最新日销量 |
| `latestPrice` | number | 最新日价格 |
| `volumeChange` | number | 销量日环比，小数值 |
| `priceChange` | number | 价格日环比，小数值 |
| `correlation` | number | 量价相关系数 |
| `trend` | SalesTrendPointDTO[] | 近 30 天走势 |
| `trendYear` | SalesTrendPointDTO[] | DTO 字段存在，当前月度接口未设置 |

`SalesTrendPointDTO`：`date`、`volume`、`price`。

### 8.2 年度量价趋势

| 项目 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/trend-analysis/yearly` |
| 权限 | `decision:trend-analysis:view` |

Query 参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `productCode` | string | 是 | 物料组编码 |
| `region` | string | 是 | Repository 查询使用的渠道值 |
| `date` | string | 是 | Controller 要求必传；当前 Service 实际使用服务器当前年份 |

响应 `data`：12 个 `SalesTrendPointDTO`，`date` 为 `yyyy-MM`。

## 9. 销售分析接口

### 9.1 公司销售列表

| 项目 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/sales-analysis/companies` |
| 权限 | `decision:sales-analysis:view` |

Query 参数：

| 字段 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| `type` | string | 否 | `amount` | `volume` 销量，`amount` 销售额 |
| `date` | date | 是 | - | 业务日期，内部统计到 `date - 1` |

响应 `data`：`CompanyMetricDTO[]`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `companyName` | string | 公司名称 |
| `value` | number | 当前指标实际值 |
| `target` | number | 当前指标预算目标 |
| `ratioText` | string | DTO 字段存在，当前服务未设置 |
| `isAlert` | boolean | DTO 字段存在，当前服务未设置 |
| `trend` | number[] | DTO 字段存在，当前服务未设置 |

### 9.2 公司销售详情

| 项目 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/sales-analysis/companies/detail` |
| 权限 | `decision:sales-analysis:view` |

Query 参数：

| 字段 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| `companyName` | string | 是 | - | 公司名称 |
| `type` | string | 否 | `amount` | `volume` 或 `amount` |
| `date` | date | 是 | - | 业务日期 |

响应 `data`：`CompanyDetailDTO[]`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `products` | ProductMetricDTO[] | 产品指标列表 |
| `dailySales` | number[] | 本月每日销售趋势 |

`ProductMetricDTO`：`productCode`、`productName`、`value`、`percentage`、`region`。

### 9.3 公司订单明细

| 项目 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/sales-analysis/orders/company-detail` |
| 权限 | `decision:sales-analysis:view` |

Query 参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `companyName` | string | 是 | 公司名称关键字 |
| `date` | date | 是 | 取 `date - 1` 所在月订单 |

响应 `data`：`OrderDetailDTO[]`

`OrderDetailDTO` 字段：`orderDate`、`orderNo`、`materialDesc`、`deliveryStatus`、`salesOrg`、`office`、`materialGroup`、`salesPerson`、`customer`、`channel`、`orderNum`、`orderAmount`、`details`。

`details` 子项字段：`amount`、`volume`、`price`、`office`、`customer`、`materialDesc`、`detailDate`。

### 9.4 产品深度分析

| 项目 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/sales-analysis/product-deep` |
| 权限 | `decision:sales-analysis:view` |

Query 参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `companyName` | string | 是 | 公司名称，会映射为公司编码 |
| `productCode` | string | 是 | 物料组编码 |
| `type` | string | 是 | `month` 或 `year`，其他值默认 `month` |
| `date` | date | 是 | 业务日期 |

响应 `data`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `kpi` | ProductDeepKPI | 汇总指标 |
| `topCustomers` | ProductDeepCustomer[] | 客户排行，最多 20 条 |
| `trend` | ProductDeepTrend[] | 趋势 |

`ProductDeepKPI`：`totalVolume`、`domesticVolume`、`intlVolume`、`totalAmount`、`domesticAmount`、`intlAmount`、`avgPrice`、`domesticAvgPrice`、`intlAvgPrice`、`profitEst`。

`ProductDeepCustomer`：`name`、`volume`、`amount`。

`ProductDeepTrend`：`date`、`domesticVolume`、`intlVolume`、`amount`、`domesticAmount`、`intlAmount`。

## 10. 回款分析接口

### 10.1 公司回款列表

| 项目 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/collection-analysis/companies` |
| 权限 | `decision:collection-analysis:view` |

Query 参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `date` | date | 是 | 内部使用 `date - 1` 所在月份 |

响应 `data`：`CompanyMetricDTO[]`

字段同销售公司列表：`companyName`、`value`、`target`、`ratioText`、`isAlert`、`trend`。

当前 Controller 只暴露 `/companies`，没有 `/company-detail`。

## 11. 三费监控接口

### 11.1 总览指标

| 项目 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/expense/overview` |
| 权限 | `decision:expense:view` |

Query 参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `date` | date | 是 | 使用 `yyyy-MM` 口径对比上月 |

响应 `data`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `totalExpense` | MetricDetail | 三费总额 |
| `salesExpense` | MetricDetail | 销售费用 |
| `managementExpense` | MetricDetail | 管理费用 |
| `financeExpense` | MetricDetail | 财务费用 |

`MetricDetail`：`amount`、`unit`、`percent`、`yoyChange`、`yoyChangeText`。

### 11.2 公司明细列表

| 项目 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/expense/company-detail` |
| 权限 | `decision:expense:view` |

Query 参数：

| 字段 | 类型 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- | --- |
| `date` | date | 是 | - | 月份口径 |
| `keyword` | string | 否 | - | 公司名称关键字 |
| `page` | number | 否 | `1` | 页码 |
| `pageSize` | number | 否 | `10` | 页大小 |

响应 `data`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `list` | Item[] | 明细列表 |
| `total` | number | 总数 |

`Item`：`name`、`sales`、`management`、`finance`、`total`、`yoy`。当前服务未设置 `yoy`。

### 11.3 公司对比

| 项目 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/expense/company-comparison` |
| 权限 | `decision:expense:view` |

Query 参数：`date` 必填。

响应 `data`：`CompanyComparisonDTO[]`，字段为 `name`、`sales`、`management`、`finance`、`total`、`yoy`。当前服务设置 `name`、`sales`、`management`、`finance`，`total` 和 `yoy` 可能为空。

### 11.4 费用结构

| 项目 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/expense/structure` |
| 权限 | `decision:expense:view` |

Query 参数：`date` 必填。

响应 `data`：`ExpenseStructureDTO[]`，字段为 `name`、`value`、`percent`。

### 11.5 三费趋势

| 项目 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/expense/trend` |
| 权限 | `decision:expense:view` |

Query 参数：`date` 必填。

响应 `data`：`ExpenseTrendDTO`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `months` | string[] | 月份轴 |
| `sales` | number[] | 销售费用趋势 |
| `management` | number[] | 管理费用趋势 |
| `finance` | number[] | 财务费用趋势 |

### 11.6 预算执行

| 项目 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/expense/budget-execution` |
| 权限 | `decision:expense:view` |

Query 参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `date` | date | 是 | 月份口径 |
| `dimension` | string | 否 | `year` 表示从当年 1 月累计到当前月，其他值按当前月 |

响应 `data`：`BudgetExecutionDTO[]`

字段：`companyName`、`salesActual`、`salesBudget`、`mgmtActual`、`mgmtBudget`、`finActual`、`finBudget`。

### 11.7 公司三费同比环比

| 项目 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/expense/growth` |
| 权限 | `decision:expense:view` |

Query 参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `date` | string | 是 | 至少包含 `yyyy-MM`，如 `2026-05` 或 `2026-05-12` |

响应 `data`：`CompanyGrowthPointDTO[]`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `companyName` | string | 公司名称 |
| `currentValue` | number | 本期值 |
| `yoyValue` | number | 去年同期值 |
| `momValue` | number | 上期值 |
| `yoy` | number | 同比增长率，百分比 |
| `mom` | number | 环比增长率，百分比 |

注意：`ExpenseService` 中存在 `getDailyDetail(date, companyName)`，但当前 `ExpenseController` 没有暴露 `/api/expense/daily-detail`。

## 12. 明细查询接口

### 12.1 销售明细

| 项目 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/all_details/sale_details` |
| 权限 | `decision:all-details:view` |

Query 参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `companyName` | string | 是 | 公司名称，映射不到时按原值传入 SQL |
| `date` | date | 是 | 过账日期 |

响应 `data`：`AllDetails[]`

字段：`businessDate`、`companyName`、`region`、`productName`、`groupName`、`sales`、`amount`、`price`。

## 13. AI 接口

### 13.1 AI 价格偏差洞察

| 项目 | 内容 |
| --- | --- |
| 方法 | `POST` |
| 路径 | `/api/dashboard/ai/insight/price-deviation` |
| 权限 | `decision:ai:insight` |
| 响应类型 | `text/event-stream` |

请求体：价格偏差图表数据数组，类型为 `List<Map<String,Object>>`。

示例：

```json
[
  {
    "productName": "产品A",
    "productCode": "A001",
    "region": "国内",
    "avgPrice7d": 1000,
    "todayPrice": 900,
    "deviationPct": -10
  }
]
```

响应为 SSE 文本流，不使用 `Result<T>` 包装。

### 13.2 AI 公司诊断

| 项目 | 内容 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/dashboard/ai/company-diagnosis` |
| 权限 | `decision:ai:diagnosis` |

Query 参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `companyName` | string | 是 | 公司名称 |
| `value` | number | 是 | 实际值 |
| `target` | number | 是 | 目标值 |
| `unit` | string | 是 | 单位 |
| `date` | date | 是 | 日期 |
| `bizType` | string | 是 | 当前服务主要处理 `sales` |

响应 `data`：`AiDiagnosisDTO`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `level` | string | `success` / `info` / `warning` |
| `icon` | string | 前端图标标识 |
| `title` | string | 诊断标题 |
| `text` | string | 诊断内容 |

## 14. 当前对接注意点

1. `/api/metrics`、`/api/metrics/orders`、`/api/price-analysis/deviations` 的 `date` 在 Controller 上允许缺省，但 Service 会直接调用日期方法，前端应始终传入。
2. `/api/price-analysis/deviations/details` 不接收 `date`，内部按服务器当前日期计算昨日或近 7 天。
3. `/api/trend-analysis/monthly` 和 `/api/trend-analysis/yearly` 要求传 `date`，但当前 Service 使用服务器当前日期计算。
4. `/api/expense/daily-detail` 当前未在 Controller 暴露。
5. `/api/collection-analysis/company-detail` 当前未在 Controller 暴露。
6. 部分 DTO 字段存在但当前服务未赋值，文档中已标注可能为空的字段。
