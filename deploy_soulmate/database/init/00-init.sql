-- ============================================
-- SoulMate 数据库初始化脚本
-- ============================================

-- 设置客户端连接字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;
SET character_set_client = utf8mb4;
SET collation_connection = utf8mb4_unicode_ci;

-- 注意：
-- 1. MySQL 容器启动时会自动创建 MYSQL_DATABASE 环境变量指定的数据库
-- 2. 开发环境: MYSQL_DATABASE=soulmate_db_dev (在 .env.dev 中配置)
-- 3. 生产环境: MYSQL_DATABASE=soulmate_db (在 .env.prod 中配置)
-- 4. 此脚本会在数据库创建后执行，用于设置字符集等初始化操作
-- 5. 后续的 01-schema.sql 和 02-data.sql 会在同一数据库中执行

