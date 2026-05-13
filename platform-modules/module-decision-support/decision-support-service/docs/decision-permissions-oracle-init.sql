-- =========================================================
-- 决策支持菜单与权限初始化脚本
-- 适用范围：补齐 module-decision-support 对应的 decision:* 权限菜单
-- 依赖：
--   1. role-oracle-schema.sql 已执行
--   2. auth-oracle-schema.sql 已执行（如需登录认证）
-- 作用：
--   1. 初始化“决策支持”菜单目录
--   2. 初始化 decision:* 权限节点
--   3. 将上述菜单权限授权给 system:admin 角色
-- 特性：
--   - 幂等，可重复执行
--   - 纯 Oracle SQL / MERGE 版本，兼容大多数 JDBC 客户端
-- =========================================================

-- 1. 决策支持根目录
MERGE INTO SYS_MENU t
USING (
  SELECT
    '/decision-support' AS PATH,
    0 AS PARENT_ID,
    '决策支持' AS MENU_NAME,
    'M' AS MENU_TYPE,
    CAST(NULL AS VARCHAR2(200)) AS COMPONENT,
    CAST(NULL AS VARCHAR2(200)) AS PERMS,
    'data-analysis' AS ICON,
    10 AS ORDER_NUM,
    1 AS STATUS,
    '决策支持根目录' AS REMARK
  FROM DUAL
) s
ON (t.PATH = s.PATH)
WHEN MATCHED THEN
  UPDATE SET
    t.PARENT_ID = s.PARENT_ID,
    t.MENU_NAME = s.MENU_NAME,
    t.MENU_TYPE = s.MENU_TYPE,
    t.COMPONENT = s.COMPONENT,
    t.PERMS = s.PERMS,
    t.ICON = s.ICON,
    t.ORDER_NUM = s.ORDER_NUM,
    t.STATUS = s.STATUS,
    t.REMARK = s.REMARK,
    t.UPDATE_BY = 'system',
    t.UPDATE_TIME = SYSDATE
WHEN NOT MATCHED THEN
  INSERT (
    MENU_ID, PARENT_ID, MENU_NAME, MENU_TYPE, PATH, COMPONENT, PERMS, ICON,
    ORDER_NUM, STATUS, REMARK, CREATE_BY, CREATE_TIME, UPDATE_BY, UPDATE_TIME
  )
  VALUES (
    SEQ_SYS_MENU.NEXTVAL, s.PARENT_ID, s.MENU_NAME, s.MENU_TYPE, s.PATH, s.COMPONENT, s.PERMS, s.ICON,
    s.ORDER_NUM, s.STATUS, s.REMARK, 'system', SYSDATE, 'system', SYSDATE
  );

-- 2. 核心指标
MERGE INTO SYS_MENU t
USING (
  SELECT
    (SELECT MENU_ID FROM SYS_MENU WHERE PATH = '/decision-support' AND ROWNUM = 1) AS PARENT_ID,
    '销售指标大盘' AS MENU_NAME,
    'C' AS MENU_TYPE,
    '/' AS PATH,
    '@/views/dashboard/DashboardView.vue' AS COMPONENT,
    'decision:metrics:view' AS PERMS,
    'histogram' AS ICON,
    1 AS ORDER_NUM,
    1 AS STATUS,
    '核心指标与订单指标查询权限' AS REMARK
  FROM DUAL
) s
ON (t.PERMS = s.PERMS)
WHEN MATCHED THEN
  UPDATE SET
    t.PARENT_ID = s.PARENT_ID,
    t.MENU_NAME = s.MENU_NAME,
    t.MENU_TYPE = s.MENU_TYPE,
    t.PATH = s.PATH,
    t.COMPONENT = s.COMPONENT,
    t.ICON = s.ICON,
    t.ORDER_NUM = s.ORDER_NUM,
    t.STATUS = s.STATUS,
    t.REMARK = s.REMARK,
    t.UPDATE_BY = 'system',
    t.UPDATE_TIME = SYSDATE
WHEN NOT MATCHED THEN
  INSERT (
    MENU_ID, PARENT_ID, MENU_NAME, MENU_TYPE, PATH, COMPONENT, PERMS, ICON,
    ORDER_NUM, STATUS, REMARK, CREATE_BY, CREATE_TIME, UPDATE_BY, UPDATE_TIME
  )
  VALUES (
    SEQ_SYS_MENU.NEXTVAL, s.PARENT_ID, s.MENU_NAME, s.MENU_TYPE, s.PATH, s.COMPONENT, s.PERMS, s.ICON,
    s.ORDER_NUM, s.STATUS, s.REMARK, 'system', SYSDATE, 'system', SYSDATE
  );

-- 3. 价格分析
MERGE INTO SYS_MENU t
USING (
  SELECT
    (SELECT MENU_ID FROM SYS_MENU WHERE PATH = '/decision-support' AND ROWNUM = 1) AS PARENT_ID,
    '价格分析' AS MENU_NAME,
    'B' AS MENU_TYPE,
    CAST(NULL AS VARCHAR2(200)) AS PATH,
    CAST(NULL AS VARCHAR2(200)) AS COMPONENT,
    'decision:price-analysis:view' AS PERMS,
    CAST(NULL AS VARCHAR2(100)) AS ICON,
    2 AS ORDER_NUM,
    1 AS STATUS,
    '价格偏差与偏差详情查询权限' AS REMARK
  FROM DUAL
) s
ON (t.PERMS = s.PERMS)
WHEN MATCHED THEN
  UPDATE SET
    t.PARENT_ID = s.PARENT_ID,
    t.MENU_NAME = s.MENU_NAME,
    t.MENU_TYPE = s.MENU_TYPE,
    t.PATH = s.PATH,
    t.COMPONENT = s.COMPONENT,
    t.ICON = s.ICON,
    t.ORDER_NUM = s.ORDER_NUM,
    t.STATUS = s.STATUS,
    t.REMARK = s.REMARK,
    t.UPDATE_BY = 'system',
    t.UPDATE_TIME = SYSDATE
WHEN NOT MATCHED THEN
  INSERT (
    MENU_ID, PARENT_ID, MENU_NAME, MENU_TYPE, PATH, COMPONENT, PERMS, ICON,
    ORDER_NUM, STATUS, REMARK, CREATE_BY, CREATE_TIME, UPDATE_BY, UPDATE_TIME
  )
  VALUES (
    SEQ_SYS_MENU.NEXTVAL, s.PARENT_ID, s.MENU_NAME, s.MENU_TYPE, s.PATH, s.COMPONENT, s.PERMS, s.ICON,
    s.ORDER_NUM, s.STATUS, s.REMARK, 'system', SYSDATE, 'system', SYSDATE
  );

-- 4. 销售分析
MERGE INTO SYS_MENU t
USING (
  SELECT
    (SELECT MENU_ID FROM SYS_MENU WHERE PATH = '/decision-support' AND ROWNUM = 1) AS PARENT_ID,
    '销售分析' AS MENU_NAME,
    'C' AS MENU_TYPE,
    '/details/sales' AS PATH,
    '@/views/sales-detail/SalesDetailView.vue' AS COMPONENT,
    'decision:sales-analysis:view' AS PERMS,
    'trend-charts' AS ICON,
    3 AS ORDER_NUM,
    1 AS STATUS,
    '销售分析与产品深度分析权限' AS REMARK
  FROM DUAL
) s
ON (t.PERMS = s.PERMS)
WHEN MATCHED THEN
  UPDATE SET
    t.PARENT_ID = s.PARENT_ID,
    t.MENU_NAME = s.MENU_NAME,
    t.MENU_TYPE = s.MENU_TYPE,
    t.PATH = s.PATH,
    t.COMPONENT = s.COMPONENT,
    t.ICON = s.ICON,
    t.ORDER_NUM = s.ORDER_NUM,
    t.STATUS = s.STATUS,
    t.REMARK = s.REMARK,
    t.UPDATE_BY = 'system',
    t.UPDATE_TIME = SYSDATE
WHEN NOT MATCHED THEN
  INSERT (
    MENU_ID, PARENT_ID, MENU_NAME, MENU_TYPE, PATH, COMPONENT, PERMS, ICON,
    ORDER_NUM, STATUS, REMARK, CREATE_BY, CREATE_TIME, UPDATE_BY, UPDATE_TIME
  )
  VALUES (
    SEQ_SYS_MENU.NEXTVAL, s.PARENT_ID, s.MENU_NAME, s.MENU_TYPE, s.PATH, s.COMPONENT, s.PERMS, s.ICON,
    s.ORDER_NUM, s.STATUS, s.REMARK, 'system', SYSDATE, 'system', SYSDATE
  );

-- 5. 趋势分析
MERGE INTO SYS_MENU t
USING (
  SELECT
    (SELECT MENU_ID FROM SYS_MENU WHERE PATH = '/decision-support' AND ROWNUM = 1) AS PARENT_ID,
    '趋势分析' AS MENU_NAME,
    'B' AS MENU_TYPE,
    CAST(NULL AS VARCHAR2(200)) AS PATH,
    CAST(NULL AS VARCHAR2(200)) AS COMPONENT,
    'decision:trend-analysis:view' AS PERMS,
    CAST(NULL AS VARCHAR2(100)) AS ICON,
    4 AS ORDER_NUM,
    1 AS STATUS,
    '销售趋势分析权限' AS REMARK
  FROM DUAL
) s
ON (t.PERMS = s.PERMS)
WHEN MATCHED THEN
  UPDATE SET
    t.PARENT_ID = s.PARENT_ID,
    t.MENU_NAME = s.MENU_NAME,
    t.MENU_TYPE = s.MENU_TYPE,
    t.PATH = s.PATH,
    t.COMPONENT = s.COMPONENT,
    t.ICON = s.ICON,
    t.ORDER_NUM = s.ORDER_NUM,
    t.STATUS = s.STATUS,
    t.REMARK = s.REMARK,
    t.UPDATE_BY = 'system',
    t.UPDATE_TIME = SYSDATE
WHEN NOT MATCHED THEN
  INSERT (
    MENU_ID, PARENT_ID, MENU_NAME, MENU_TYPE, PATH, COMPONENT, PERMS, ICON,
    ORDER_NUM, STATUS, REMARK, CREATE_BY, CREATE_TIME, UPDATE_BY, UPDATE_TIME
  )
  VALUES (
    SEQ_SYS_MENU.NEXTVAL, s.PARENT_ID, s.MENU_NAME, s.MENU_TYPE, s.PATH, s.COMPONENT, s.PERMS, s.ICON,
    s.ORDER_NUM, s.STATUS, s.REMARK, 'system', SYSDATE, 'system', SYSDATE
  );

-- 6. 回款分析
MERGE INTO SYS_MENU t
USING (
  SELECT
    (SELECT MENU_ID FROM SYS_MENU WHERE PATH = '/decision-support' AND ROWNUM = 1) AS PARENT_ID,
    '回款分析' AS MENU_NAME,
    'C' AS MENU_TYPE,
    '/details/collection' AS PATH,
    '@/views/collection-detail/CollectionDetailView.vue' AS COMPONENT,
    'decision:collection-analysis:view' AS PERMS,
    'money' AS ICON,
    5 AS ORDER_NUM,
    1 AS STATUS,
    '回款分析权限' AS REMARK
  FROM DUAL
) s
ON (t.PERMS = s.PERMS)
WHEN MATCHED THEN
  UPDATE SET
    t.PARENT_ID = s.PARENT_ID,
    t.MENU_NAME = s.MENU_NAME,
    t.MENU_TYPE = s.MENU_TYPE,
    t.PATH = s.PATH,
    t.COMPONENT = s.COMPONENT,
    t.ICON = s.ICON,
    t.ORDER_NUM = s.ORDER_NUM,
    t.STATUS = s.STATUS,
    t.REMARK = s.REMARK,
    t.UPDATE_BY = 'system',
    t.UPDATE_TIME = SYSDATE
WHEN NOT MATCHED THEN
  INSERT (
    MENU_ID, PARENT_ID, MENU_NAME, MENU_TYPE, PATH, COMPONENT, PERMS, ICON,
    ORDER_NUM, STATUS, REMARK, CREATE_BY, CREATE_TIME, UPDATE_BY, UPDATE_TIME
  )
  VALUES (
    SEQ_SYS_MENU.NEXTVAL, s.PARENT_ID, s.MENU_NAME, s.MENU_TYPE, s.PATH, s.COMPONENT, s.PERMS, s.ICON,
    s.ORDER_NUM, s.STATUS, s.REMARK, 'system', SYSDATE, 'system', SYSDATE
  );

-- 7. 费用分析
MERGE INTO SYS_MENU t
USING (
  SELECT
    (SELECT MENU_ID FROM SYS_MENU WHERE PATH = '/decision-support' AND ROWNUM = 1) AS PARENT_ID,
    '三费监控' AS MENU_NAME,
    'C' AS MENU_TYPE,
    '/expense-monitor' AS PATH,
    '@/views/expense-monitor/ExpenseMonitorView.vue' AS COMPONENT,
    'decision:expense:view' AS PERMS,
    'pie-chart' AS ICON,
    6 AS ORDER_NUM,
    1 AS STATUS,
    '费用分析权限' AS REMARK
  FROM DUAL
) s
ON (t.PERMS = s.PERMS)
WHEN MATCHED THEN
  UPDATE SET
    t.PARENT_ID = s.PARENT_ID,
    t.MENU_NAME = s.MENU_NAME,
    t.MENU_TYPE = s.MENU_TYPE,
    t.PATH = s.PATH,
    t.COMPONENT = s.COMPONENT,
    t.ICON = s.ICON,
    t.ORDER_NUM = s.ORDER_NUM,
    t.STATUS = s.STATUS,
    t.REMARK = s.REMARK,
    t.UPDATE_BY = 'system',
    t.UPDATE_TIME = SYSDATE
WHEN NOT MATCHED THEN
  INSERT (
    MENU_ID, PARENT_ID, MENU_NAME, MENU_TYPE, PATH, COMPONENT, PERMS, ICON,
    ORDER_NUM, STATUS, REMARK, CREATE_BY, CREATE_TIME, UPDATE_BY, UPDATE_TIME
  )
  VALUES (
    SEQ_SYS_MENU.NEXTVAL, s.PARENT_ID, s.MENU_NAME, s.MENU_TYPE, s.PATH, s.COMPONENT, s.PERMS, s.ICON,
    s.ORDER_NUM, s.STATUS, s.REMARK, 'system', SYSDATE, 'system', SYSDATE
  );

-- 8. 明细查询
MERGE INTO SYS_MENU t
USING (
  SELECT
    (SELECT MENU_ID FROM SYS_MENU WHERE PATH = '/decision-support' AND ROWNUM = 1) AS PARENT_ID,
    '明细数据查询' AS MENU_NAME,
    'C' AS MENU_TYPE,
    '/all-details' AS PATH,
    '@/views/all-details/AllDetailsView.vue' AS COMPONENT,
    'decision:all-details:view' AS PERMS,
    'document' AS ICON,
    7 AS ORDER_NUM,
    1 AS STATUS,
    '销售明细查询权限' AS REMARK
  FROM DUAL
) s
ON (t.PERMS = s.PERMS)
WHEN MATCHED THEN
  UPDATE SET
    t.PARENT_ID = s.PARENT_ID,
    t.MENU_NAME = s.MENU_NAME,
    t.MENU_TYPE = s.MENU_TYPE,
    t.PATH = s.PATH,
    t.COMPONENT = s.COMPONENT,
    t.ICON = s.ICON,
    t.ORDER_NUM = s.ORDER_NUM,
    t.STATUS = s.STATUS,
    t.REMARK = s.REMARK,
    t.UPDATE_BY = 'system',
    t.UPDATE_TIME = SYSDATE
WHEN NOT MATCHED THEN
  INSERT (
    MENU_ID, PARENT_ID, MENU_NAME, MENU_TYPE, PATH, COMPONENT, PERMS, ICON,
    ORDER_NUM, STATUS, REMARK, CREATE_BY, CREATE_TIME, UPDATE_BY, UPDATE_TIME
  )
  VALUES (
    SEQ_SYS_MENU.NEXTVAL, s.PARENT_ID, s.MENU_NAME, s.MENU_TYPE, s.PATH, s.COMPONENT, s.PERMS, s.ICON,
    s.ORDER_NUM, s.STATUS, s.REMARK, 'system', SYSDATE, 'system', SYSDATE
  );

-- 9. AI 价格洞察
MERGE INTO SYS_MENU t
USING (
  SELECT
    (SELECT MENU_ID FROM SYS_MENU WHERE PATH = '/decision-support' AND ROWNUM = 1) AS PARENT_ID,
    'AI价格洞察' AS MENU_NAME,
    'B' AS MENU_TYPE,
    CAST(NULL AS VARCHAR2(200)) AS PATH,
    CAST(NULL AS VARCHAR2(200)) AS COMPONENT,
    'decision:ai:insight' AS PERMS,
    CAST(NULL AS VARCHAR2(100)) AS ICON,
    8 AS ORDER_NUM,
    1 AS STATUS,
    'AI价格偏差洞察权限' AS REMARK
  FROM DUAL
) s
ON (t.PERMS = s.PERMS)
WHEN MATCHED THEN
  UPDATE SET
    t.PARENT_ID = s.PARENT_ID,
    t.MENU_NAME = s.MENU_NAME,
    t.MENU_TYPE = s.MENU_TYPE,
    t.PATH = s.PATH,
    t.COMPONENT = s.COMPONENT,
    t.ICON = s.ICON,
    t.ORDER_NUM = s.ORDER_NUM,
    t.STATUS = s.STATUS,
    t.REMARK = s.REMARK,
    t.UPDATE_BY = 'system',
    t.UPDATE_TIME = SYSDATE
WHEN NOT MATCHED THEN
  INSERT (
    MENU_ID, PARENT_ID, MENU_NAME, MENU_TYPE, PATH, COMPONENT, PERMS, ICON,
    ORDER_NUM, STATUS, REMARK, CREATE_BY, CREATE_TIME, UPDATE_BY, UPDATE_TIME
  )
  VALUES (
    SEQ_SYS_MENU.NEXTVAL, s.PARENT_ID, s.MENU_NAME, s.MENU_TYPE, s.PATH, s.COMPONENT, s.PERMS, s.ICON,
    s.ORDER_NUM, s.STATUS, s.REMARK, 'system', SYSDATE, 'system', SYSDATE
  );

-- 10. AI 公司诊断
MERGE INTO SYS_MENU t
USING (
  SELECT
    (SELECT MENU_ID FROM SYS_MENU WHERE PATH = '/decision-support' AND ROWNUM = 1) AS PARENT_ID,
    'AI公司诊断' AS MENU_NAME,
    'B' AS MENU_TYPE,
    CAST(NULL AS VARCHAR2(200)) AS PATH,
    CAST(NULL AS VARCHAR2(200)) AS COMPONENT,
    'decision:ai:diagnosis' AS PERMS,
    CAST(NULL AS VARCHAR2(100)) AS ICON,
    9 AS ORDER_NUM,
    1 AS STATUS,
    'AI公司经营诊断权限' AS REMARK
  FROM DUAL
) s
ON (t.PERMS = s.PERMS)
WHEN MATCHED THEN
  UPDATE SET
    t.PARENT_ID = s.PARENT_ID,
    t.MENU_NAME = s.MENU_NAME,
    t.MENU_TYPE = s.MENU_TYPE,
    t.PATH = s.PATH,
    t.COMPONENT = s.COMPONENT,
    t.ICON = s.ICON,
    t.ORDER_NUM = s.ORDER_NUM,
    t.STATUS = s.STATUS,
    t.REMARK = s.REMARK,
    t.UPDATE_BY = 'system',
    t.UPDATE_TIME = SYSDATE
WHEN NOT MATCHED THEN
  INSERT (
    MENU_ID, PARENT_ID, MENU_NAME, MENU_TYPE, PATH, COMPONENT, PERMS, ICON,
    ORDER_NUM, STATUS, REMARK, CREATE_BY, CREATE_TIME, UPDATE_BY, UPDATE_TIME
  )
  VALUES (
    SEQ_SYS_MENU.NEXTVAL, s.PARENT_ID, s.MENU_NAME, s.MENU_TYPE, s.PATH, s.COMPONENT, s.PERMS, s.ICON,
    s.ORDER_NUM, s.STATUS, s.REMARK, 'system', SYSDATE, 'system', SYSDATE
  );

-- 11. 授权给 system:admin 角色
INSERT INTO SYS_ROLE_MENU (ROLE_ID, MENU_ID, CREATE_TIME)
SELECT r.ROLE_ID, m.MENU_ID, SYSDATE
  FROM SYS_ROLE r
  JOIN SYS_MENU m
    ON m.PATH = '/decision-support'
    OR m.PERMS IN (
      'decision:metrics:view',
      'decision:price-analysis:view',
      'decision:sales-analysis:view',
      'decision:trend-analysis:view',
      'decision:collection-analysis:view',
      'decision:expense:view',
      'decision:all-details:view',
      'decision:ai:insight',
      'decision:ai:diagnosis'
    )
 WHERE r.ROLE_KEY = 'system:admin'
   AND NOT EXISTS (
     SELECT 1
       FROM SYS_ROLE_MENU rm
      WHERE rm.ROLE_ID = r.ROLE_ID
        AND rm.MENU_ID = m.MENU_ID
   );

COMMIT;

-- 可选检查：
-- SELECT MENU_ID, PARENT_ID, MENU_NAME, MENU_TYPE, PATH, PERMS, ORDER_NUM
--   FROM SYS_MENU
--  WHERE PATH = '/decision-support'
--     OR PERMS LIKE 'decision:%'
--  ORDER BY PARENT_ID, ORDER_NUM, MENU_ID;

-- SELECT R.ROLE_KEY, M.MENU_NAME, M.PERMS
--   FROM SYS_ROLE_MENU RM
--   JOIN SYS_ROLE R ON R.ROLE_ID = RM.ROLE_ID
--   JOIN SYS_MENU M ON M.MENU_ID = RM.MENU_ID
--  WHERE R.ROLE_KEY = 'system:admin'
--    AND (M.PERMS LIKE 'decision:%' OR M.PATH = '/decision-support')
--  ORDER BY M.ORDER_NUM, M.MENU_ID;
