-- DingTalk passwordless login fields for SYS_USER.
-- Run this before enabling POST /api/v1/auth/dingtalk/login.

ALTER TABLE SYS_USER ADD (
  DING_USER_ID VARCHAR2(64),
  DING_UNION_ID VARCHAR2(64)
);

COMMENT ON COLUMN SYS_USER.DING_USER_ID IS 'DingTalk userId for passwordless login binding';
COMMENT ON COLUMN SYS_USER.DING_UNION_ID IS 'DingTalk unionId, reserved for cross-app identity binding';

CREATE UNIQUE INDEX UK_SYS_USER_DING_USER_ID ON SYS_USER(DING_USER_ID);
CREATE INDEX IDX_SYS_USER_MOBILE ON SYS_USER(MOBILE);

-- Optional one-time binding examples:
-- UPDATE SYS_USER SET DING_USER_ID = '<ding_user_id>' WHERE USERNAME = '<username>';
-- UPDATE SYS_USER SET DING_USER_ID = '<ding_user_id>' WHERE MOBILE = '<mobile>';
