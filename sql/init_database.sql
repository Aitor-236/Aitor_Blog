-- ============================================================
-- Aitor Blog：数据库初始化脚本（全新部署用）
-- 用途：从零创建 blog_db、系统用户表和文章模块全部表，可重复执行。
-- 前提：
--   1. MySQL 8+
--   2. 执行账号需要有 CREATE DATABASE 权限
-- 用法：
--   mysql -uroot -p < sql/init_database.sql
-- 说明：
--   - 本脚本不预置任何用户，也不写入任何文章数据。
--     执行完后请运行 sql/init_account.sh 创建你自己的登录账号。
--   - 如果数据库已存在、只是要补文章模块的表，用 sql/article_schema.sql。
--   - 全部使用 CREATE TABLE IF NOT EXISTS，重复执行不会报错，也不会覆盖已有数据。
--   - 注意：文章模块表结构变更时，本文件与 article_schema.sql 需要同步修改。
-- ============================================================

CREATE DATABASE IF NOT EXISTS blog_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE blog_db;

-- ------------------------------------------------------------
-- 系统用户表（登录模块使用）
-- 密码列存 BCrypt 哈希（60 字符，$2b$ 前缀），绝不存明文。
-- 本表刻意不预置任何账号，部署时由 sql/init_account.sh 创建。
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    email VARCHAR(100) NOT NULL COMMENT '邮箱',
    password VARCHAR(100) NOT NULL COMMENT '密码（BCrypt 哈希）',
    avatar VARCHAR(255) NOT NULL DEFAULT '' COMMENT '头像地址，默认为空',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统用户表';

-- 文章分类表
CREATE TABLE IF NOT EXISTS article_category (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    slug VARCHAR(50) NOT NULL COMMENT '分类英文标识，用于稳定筛选',
    sort_order INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_article_category_slug (slug)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文章分类表';

-- 文章标签表
CREATE TABLE IF NOT EXISTS tag (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    name VARCHAR(50) NOT NULL COMMENT '标签名称',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tag_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文章标签表';

-- 文章主表
CREATE TABLE IF NOT EXISTS article (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '文章ID',
    author_id BIGINT NOT NULL COMMENT '作者ID，关联 sys_user.id（与 sys_user.id 同为有符号 BIGINT）',
    category_id BIGINT UNSIGNED NOT NULL COMMENT '主分类ID，关联 article_category.id',
    title VARCHAR(200) NOT NULL COMMENT '文章标题',
    summary VARCHAR(500) NOT NULL COMMENT '文章摘要，用于列表卡片展示',
    content_markdown LONGTEXT NOT NULL COMMENT 'Markdown 正文原文',
    reading_minutes INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '预计阅读时长（分钟）',
    status VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态：draft-草稿，published-已发布',
    published_at DATETIME NULL COMMENT '发布时间，发布时写入',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_article_status_published (status, published_at),
    KEY idx_article_author_id (author_id),
    KEY idx_article_category_id (category_id),
    CONSTRAINT fk_article_author FOREIGN KEY (author_id)
        REFERENCES sys_user (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    CONSTRAINT fk_article_category FOREIGN KEY (category_id)
        REFERENCES article_category (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文章主表';

-- 文章-标签关联表
CREATE TABLE IF NOT EXISTS article_tag (
    article_id BIGINT UNSIGNED NOT NULL COMMENT '文章ID',
    tag_id BIGINT UNSIGNED NOT NULL COMMENT '标签ID',
    PRIMARY KEY (article_id, tag_id),
    KEY idx_article_tag_tag_id (tag_id),
    CONSTRAINT fk_article_tag_article FOREIGN KEY (article_id)
        REFERENCES article (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_article_tag_tag FOREIGN KEY (tag_id)
        REFERENCES tag (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文章与标签关联表';

-- 预置与现有文章页面一致的分类数据（基于 slug 幂等）
INSERT INTO article_category (name, slug, sort_order)
VALUES
    ('前端', 'frontend', 10),
    ('后端', 'backend', 20),
    ('绘画', 'painting', 30),
    ('生活', 'life', 40)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    sort_order = VALUES(sort_order);
