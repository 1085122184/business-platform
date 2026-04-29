# Decision Support 权限结构整理

## 结论

当前登录后出现 `Access Denied` 的直接原因是：

1. 后端权限判断依赖 `@PreAuthorize("@ss.hasPermi('...')")`
2. 登录成功后返回的 `permissions` 来自 `SYS_MENU.PERMS`
3. 现有初始化脚本只有 `system:role:*`
4. `module-decision-support` 新增的大部分接口使用的是 `decision:*`
5. 角色没有分配这些 `decision:*` 权限时，接口会直接返回 `403 Access Denied`

## 认证接口

这组接口不需要菜单权限点：

| 接口 | 方法 | 说明 | 权限要求 |
| --- | --- | --- | --- |
| `/api/v1/auth/login` | `POST` | 登录 | 放行 |
| `/api/v1/auth/logout` | `POST` | 退出登录 | 仅登录态 |
| `/api/v1/auth/profile` | `GET` | 获取当前用户信息 | 仅登录态 |

## 角色管理接口

| 权限标识 | 接口 | 方法 | 说明 |
| --- | --- | --- | --- |
| `system:role:list` | `/api/v1/system/roles` | `GET` | 分页查询角色 |
| `system:role:add` | `/api/v1/system/roles` | `POST` | 新增角色 |
| `system:role:edit` | `/api/v1/system/roles` | `PUT` | 修改角色 |
| `system:role:remove` | `/api/v1/system/roles/{id}` | `DELETE` | 删除角色 |
| `system:role:list` | `/api/v1/system/roles/menu-tree` | `GET` | 获取菜单树 |
| `system:role:grant` | `/api/v1/system/roles/{roleId}/menus` | `GET` | 查询角色菜单 |
| `system:role:grant` | `/api/v1/system/roles/{roleId}/menus` | `PUT` | 保存角色菜单权限 |

## 决策支持接口

### 1. 核心指标

权限：`decision:metrics:view`

| 接口 | 方法 | 说明 |
| --- | --- | --- |
| `/api/metrics` | `GET` | 核心指标 |
| `/api/metrics/orders` | `GET` | 订单指标 |

### 2. 价格分析

权限：`decision:price-analysis:view`

| 接口 | 方法 | 说明 |
| --- | --- | --- |
| `/api/price-analysis/deviations` | `GET` | 价格偏差列表 |
| `/api/price-analysis/deviations/details` | `GET` | 价格偏差详情 |

### 3. 销售分析

权限：`decision:sales-analysis:view`

| 接口 | 方法 | 说明 |
| --- | --- | --- |
| `/api/sales-analysis/companies` | `GET` | 公司销售列表 |
| `/api/sales-analysis/companies/detail` | `GET` | 公司销售明细 |
| `/api/sales-analysis/orders/company-detail` | `GET` | 公司订单明细 |
| `/api/sales-analysis/product-deep` | `GET` | 产品深度分析 |

### 4. 趋势分析

权限：`decision:trend-analysis:view`

| 接口 | 方法 | 说明 |
| --- | --- | --- |
| `/api/trend-analysis/monthly` | `GET` | 月度趋势 |
| `/api/trend-analysis/yearly` | `GET` | 年度趋势 |

### 5. 回款分析

权限：`decision:collection-analysis:view`

| 接口 | 方法 | 说明 |
| --- | --- | --- |
| `/api/collection-analysis/companies` | `GET` | 回款公司列表 |

### 6. 费用分析

权限：`decision:expense:view`

| 接口 | 方法 | 说明 |
| --- | --- | --- |
| `/api/expense/overview` | `GET` | 费用总览 |
| `/api/expense/company-detail` | `GET` | 公司费用明细 |
| `/api/expense/company-comparison` | `GET` | 公司费用对比 |
| `/api/expense/structure` | `GET` | 费用结构 |
| `/api/expense/trend` | `GET` | 费用趋势 |
| `/api/expense/budget-execution` | `GET` | 预算执行 |
| `/api/expense/growth` | `GET` | 增长点 |

### 7. 明细查询

权限：`decision:all-details:view`

| 接口 | 方法 | 说明 |
| --- | --- | --- |
| `/api/all_details/sale_details` | `GET` | 销售明细 |

### 8. AI 分析

| 权限标识 | 接口 | 方法 | 说明 |
| --- | --- | --- | --- |
| `decision:ai:insight` | `/api/dashboard/ai/insight/price-deviation` | `POST` | AI 价格偏差洞察 |
| `decision:ai:diagnosis` | `/api/dashboard/ai/company-diagnosis` | `GET` | AI 公司诊断 |

## 建议的菜单权限结构

下面这套结构可以直接作为 `SYS_MENU` 的规划基础：

```text
系统管理
  角色管理
    角色查询       system:role:list
    角色新增       system:role:add
    角色编辑       system:role:edit
    角色删除       system:role:remove
    角色授权       system:role:grant

决策支持
  核心指标         decision:metrics:view
  价格分析         decision:price-analysis:view
  销售分析         decision:sales-analysis:view
  趋势分析         decision:trend-analysis:view
  回款分析         decision:collection-analysis:view
  费用分析         decision:expense:view
  销售明细         decision:all-details:view
  AI 价格洞察      decision:ai:insight
  AI 公司诊断      decision:ai:diagnosis
```

## 建议的授权原则

### 管理员

- 角色管理全量权限
- 决策支持全量权限

### 普通业务用户

- 只授予需要访问的决策模块权限
- 不授予 `system:role:*`

### AI 使用者

- 至少授予 `decision:ai:insight`
- 如果需要公司诊断，再授予 `decision:ai:diagnosis`

## 为什么登录后会 403

如果当前登录返回的是：

```json
{
  "roles": ["system:admin"],
  "permissions": [
    "system:role:list",
    "system:role:add",
    "system:role:edit",
    "system:role:remove",
    "system:role:grant"
  ]
}
```

那么以下接口都会被拒绝：

- `/api/metrics`
- `/api/price-analysis/**`
- `/api/sales-analysis/**`
- `/api/trend-analysis/**`
- `/api/collection-analysis/**`
- `/api/expense/**`
- `/api/all_details/**`
- `/api/dashboard/ai/**`

因为它们要求的是 `decision:*`，而不是 `system:role:*`。

## 落地建议

1. 在 `SYS_MENU` 中补齐所有 `decision:*` 权限节点
2. 给管理员角色分配这些菜单权限
3. 给其他业务角色按需分配
4. 重新登录，确认登录返回的 `permissions` 已包含对应 `decision:*`
5. 再访问相关接口
