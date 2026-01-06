# SoulMate 数据库部署说明

## 环境配置

### 1. 创建环境变量文件

**开发环境**：
```bash
cp .env.dev.example .env.dev
# 编辑 .env.dev，修改密码等配置
```

**生产环境**：
```bash
cp .env.prod.example .env.prod
# 编辑 .env.prod，修改密码等配置（必须使用强密码）
```

### 2. 环境变量说明

| 变量 | 开发环境 | 生产环境 | 说明 |
|-----|---------|---------|------|
| `ENV` | `dev` | `prod` | 环境标识 |
| `MYSQL_DATABASE` | `soulmate_db_dev` | `soulmate_db` | 数据库名称（开发环境带dev后缀） |
| `MYSQL_ROOT_PASSWORD` | 简单密码 | 强密码 | MySQL root密码 |
| `MYSQL_USER` | `soulmate_dev` | `soulmate_prod` | MySQL用户 |
| `MYSQL_PASSWORD` | 简单密码 | 强密码 | MySQL用户密码 |

## 启动方式

### 开发环境
```bash
cd deploy_soulmate
# 1. 创建环境变量文件（如果还没有）
cp .env.dev.example .env.dev
# 编辑 .env.dev，修改密码等配置

# 2. 一键启动
docker-compose -f docker-compose-dev.yml --env-file .env.dev up -d

# 3. 查看状态
docker-compose -f docker-compose-dev.yml ps

# 4. 停止服务
docker-compose -f docker-compose-dev.yml down
```

### 生产环境
```bash
cd deploy_soulmate
# 1. 创建环境变量文件（如果还没有）
cp .env.prod.example .env.prod
# 编辑 .env.prod，修改密码等配置（必须使用强密码）

# 2. 一键启动
docker-compose -f docker-compose-prod.yml --env-file .env.prod up -d

# 3. 查看状态
docker-compose -f docker-compose-prod.yml ps

# 4. 停止服务
docker-compose -f docker-compose-prod.yml down
```

## 数据库初始化

MySQL 容器首次启动时会自动执行以下脚本（按顺序）：

1. **00-init.sql** - 数据库初始化（设置字符集等）
2. **01-schema.sql** - 创建所有表结构
3. **02-data.sql** - 插入初始化数据（用户、模型、智能体等）

## 访问数据库

### MySQL 命令行
```bash
# 开发环境
docker exec -it soulmate-mysql-dev mysql -u soulmate_dev -p

# 生产环境
docker exec -it soulmate-mysql-prod mysql -u soulmate_prod -p
```

### phpMyAdmin
访问 http://localhost:8081（开发环境）或配置的端口

## 初始化数据说明

### 用户
- **管理员**: `admin` / `admin123`
- **测试用户**: `demo` / `user123`

### LLM 模型
- DeepSeek Chat
- GLM-4 Flash
- 通义千问 Turbo
- 智谱 Embedding（向量模型）

### 智能体（已上架）
1. **暖心陪伴** - 通用心理陪伴
2. **职场解压师** - 专注职场压力管理
3. **情感倾听者** - 专注情感问题
4. **睡眠守护者** - 专注睡眠和焦虑
5. **成长伙伴** - 专注个人成长

### 知识库
- 心理学基础知识库
- 情绪管理知识库
- 职场心理知识库
- 睡眠与焦虑知识库
- 情感关系知识库

### 工作流
- 完整心理陪伴工作流（包含所有节点）
- 简单陪伴工作流（无RAG）
- 情绪识别工作流（专注情绪疏导）

## 注意事项

1. **首次启动**：确保 `.env.dev` 或 `.env.prod` 文件已创建并配置正确
2. **数据持久化**：数据存储在 Docker volumes 中，删除容器不会丢失数据
3. **密码安全**：生产环境必须使用强密码
4. **端口冲突**：确保 3306、6379、8081 端口未被占用

