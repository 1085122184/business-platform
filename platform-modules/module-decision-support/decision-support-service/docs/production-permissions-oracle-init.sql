-- =========================================================
-- Production module menu and permission initialization.
--
-- Run after decision-permissions-oracle-init.sql.
-- Idempotent Oracle script. It grants production permissions to system:admin.
-- =========================================================

MERGE INTO SYS_MENU t
USING (
  SELECT
    (SELECT MENU_ID FROM SYS_MENU WHERE PATH = '/decision-support' AND ROWNUM = 1) AS PARENT_ID,
    '生产运营指标大盘' AS MENU_NAME,
    'C' AS MENU_TYPE,
    '/production-dashboard' AS PATH,
    '@/views/production/ProductionDashboardView.vue' AS COMPONENT,
    'decision:production:view' AS PERMS,
    'factory' AS ICON,
    10 AS ORDER_NUM,
    1 AS STATUS,
    '生产运营总览、产量、原料消耗、库存和吞吐指标查看权限' AS REMARK
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

MERGE INTO SYS_MENU t
USING (
  SELECT
    (SELECT MENU_ID FROM SYS_MENU WHERE PATH = '/decision-support' AND ROWNUM = 1) AS PARENT_ID,
    '生产明细数据' AS MENU_NAME,
    'C' AS MENU_TYPE,
    '/production-detail' AS PATH,
    '@/views/production/ProductionDetailView.vue' AS COMPONENT,
    'decision:production-detail:view' AS PERMS,
    'document' AS ICON,
    11 AS ORDER_NUM,
    1 AS STATUS,
    '生产产量、原料消耗、车辆运输明细查看权限' AS REMARK
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

INSERT INTO SYS_ROLE_MENU (ROLE_ID, MENU_ID, CREATE_TIME)
SELECT r.ROLE_ID, m.MENU_ID, SYSDATE
  FROM SYS_ROLE r
  JOIN SYS_MENU m
    ON m.PERMS IN (
      'decision:production:view',
      'decision:production-detail:view'
    )
 WHERE r.ROLE_KEY = 'system:admin'
   AND NOT EXISTS (
     SELECT 1
       FROM SYS_ROLE_MENU rm
      WHERE rm.ROLE_ID = r.ROLE_ID
        AND rm.MENU_ID = m.MENU_ID
   );

COMMIT;
