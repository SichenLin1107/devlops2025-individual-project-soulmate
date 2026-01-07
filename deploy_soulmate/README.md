# SoulMate 心伴 - 部署指南

## 前置要求

- Docker & Docker Compose
- Linux / macOS / Windows (WSL 或 Git Bash)

---

## 开发环境部署

```bash
cd deploy_soulmate

# 1. 创建环境变量文件
cp env.dev.example .env.dev

# 2. 启动所有服务（二选一）
make dev
# 或
bash deploy.dev.sh

# 3. 查看服务启动状态
docker ps --filter "name=soulmate-dev"
```

> ⏱️ **预计等待时间**：首次部署约 3-5 分钟（需构建镜像），后续启动约 1-2 分钟

**访问地址：**

| 服务 | 地址 |
|------|------|
| 前端 | http://localhost:3000 |
| 后端 API | http://localhost:8081 |
| Swagger 文档 | http://localhost:8081/swagger-ui.html |
| RAG 服务 | http://localhost:8000 |
| RAG API 文档 | http://localhost:8000/docs |
| phpMyAdmin | http://localhost:8082 |
| MySQL | localhost:3307 |

---

## 生产环境部署

```bash
cd deploy_soulmate

# 1. 创建环境变量文件
cp env.prod.example .env.prod

# 2. 修改 .env.prod 中的敏感配置（必须修改）
#    - MYSQL_ROOT_PASSWORD
#    - JWT_SECRET

# 3. 启动所有服务（二选一）
make prod
# 或
bash deploy.prod.sh

# 4. 查看服务启动状态
docker ps --filter "name=soulmate"
```

> ⏱️ **预计等待时间**：首次部署约 3-5 分钟（需构建镜像），后续启动约 1-2 分钟

**访问地址：**

| 服务 | 地址 |
|------|------|
| 前端 | http://localhost |
| 后端 API | http://localhost:8080 |
| RAG 服务 | http://localhost:8001 |
| phpMyAdmin | http://localhost:8083 |
| MySQL | localhost:3306 |

---

## 常用命令

| 命令 | 说明 |
|------|------|
| `make dev` | 启动开发环境 |
| `make prod` | 启动生产环境 |
| `make stop-dev` | 停止开发环境 |
| `make stop-prod` | 停止生产环境 |
| `make stop` | 停止所有环境 |
| `make logs-dev` | 查看开发环境日志 |
| `make logs-prod` | 查看生产环境日志 |
| `make dev-rag-only` | 只启动 MySQL + phpMyAdmin + RAG（本地开发前后端时使用） |

---

## Windows 用户（无 make 环境）

Windows 下如果没有 `make`，可直接使用 `docker compose` 命令：

**开发环境：**

```powershell
# 启动
docker compose -f docker-compose-dev.yml --env-file .env.dev up -d --build

# 查看状态
docker ps --filter "name=soulmate-dev"

# 查看日志
docker compose -f docker-compose-dev.yml --env-file .env.dev logs -f

# 停止
docker compose -f docker-compose-dev.yml --env-file .env.dev down
```

**生产环境：**

```powershell
# 启动
docker compose -f docker-compose-prod.yml --env-file .env.prod up -d --build

# 查看状态
docker ps --filter "name=soulmate-"

# 查看日志
docker compose -f docker-compose-prod.yml --env-file .env.prod logs -f

# 停止
docker compose -f docker-compose-prod.yml --env-file .env.prod down
```

---

## 端口配置

| 服务 | 开发环境 | 生产环境 |
|------|----------|----------|
| MySQL | 3307 | 3306 |
| Backend | 8081 | 8080 |
| Frontend | 3000 | 80 |
| RAG Service | 8000 | 8001 |
| phpMyAdmin | 8082 | 8083 |

---

## 默认账号

- 用户名：`admin`
- 密码：`admin123`

---

## 故障排查

```bash
# 检查容器状态
docker ps --filter "name=soulmate"

# 查看所有容器（包括已停止的）
docker ps -a --filter "name=soulmate"

# 查看指定容器日志
docker logs soulmate-dev-backend
docker logs soulmate-dev-mysql
docker logs soulmate-dev-rag-service
docker logs soulmate-dev-frontend

# 进入容器内部调试
docker exec -it soulmate-dev-backend sh
```
