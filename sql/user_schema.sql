-- ============================================================
-- Aitor Blog：用户（登录）模块增量升级脚本
-- 用途：给已有的 blog_db 补齐用户模块后续新增的列，可重复执行。
-- 前提：
--   1. MySQL 8+
--   2. blog_db 与 sys_user 表已存在（全新部署请直接用 sql/init_database.sql）
-- 用法：
--   mysql -uroot -p < sql/user_schema.sql
-- 说明：
--   - MySQL 8 不支持 ADD COLUMN IF NOT EXISTS，这里先查 information_schema 再执行 DDL，重复执行不报错。
--   - 本文件只做增量升级，字段定义需与 sql/init_database.sql 中的 sys_user 保持一致。
--   - 已完成升级的库执行本脚本不会修改任何数据。
-- ============================================================

USE blog_db;

-- ------------------------------------------------------------
-- sys_user.avatar：头像地址，默认空字符串表示未设置头像
-- ------------------------------------------------------------
SET @avatar_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user'
      AND COLUMN_NAME = 'avatar'
);

SET @avatar_ddl := IF(
    @avatar_exists = 0,
    'ALTER TABLE sys_user ADD COLUMN avatar VARCHAR(255) NOT NULL DEFAULT '''' COMMENT ''头像地址，默认为空'' AFTER password',
    'SELECT ''sys_user.avatar 已存在，跳过'' AS message'
);

PREPARE avatar_stmt FROM @avatar_ddl;
EXECUTE avatar_stmt;
DEALLOCATE PREPARE avatar_stmt;
