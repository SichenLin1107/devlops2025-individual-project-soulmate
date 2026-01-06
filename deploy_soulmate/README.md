# SoulMate 心伴 - 部署指南

## ⚙️ 快速开始

```bash
cd deploy_soulmate

# 初始化环境变量
cp env.dev.example .env.dev
cp env.prod.example .env.prod

# ⚠️ 生产环境必须修改 .env.prod 中的密码和密钥
```

确保已安装 Docker / Docker Compose。

---

## 🚀 启动服务

### 开发环境

**Linux/macOS：** `make dev` 或 `bash deploy.dev.sh`  
**Windows：** `deploy.dev.bat`

**访问地址：**
- 前端：http://localhost:3000
- 后端：http://localhost:8081 | Swagger: http://localhost:8081/swagger-ui.html
- RAG 服务：http://localhost:8000 | Docs: http://localhost:8000/docs
- phpMyAdmin：http://localhost:8082
- MySQL：localhost:3307

### 生产环境

**Linux/macOS：** `make prod` 或 `bash deploy.prod.sh`  
**Windows：** `deploy.prod.bat`

**访问地址：**
- 前端：http://localhost
- 后端：http://localhost:8080
- RAG 服务：http://localhost:8001 | Docs: http://localhost:8001/docs
- phpMyAdmin：http://localhost:8083
- MySQL：localhost:3306

---

## 📋 配置说明

### 端口配置

| 服务 | 开发环境 | 生产环境 |
|------|----------|----------|
| MySQL | 3307 | 3306 |
| Backend | 8081 | 8080 |
| Frontend | 3000 | 80 |
| RAG Service | 8000 | 8001 |
| phpMyAdmin | 8082 | 8083 |

### 服务间通信（Docker 网络）

| 调用方 → 目标 | 开发环境 | 生产环境 |
|--------------|----------|----------|
| Backend → RAG | `http://rag-service:8000` | `http://rag-service:8001` |
| Frontend → Backend | `http://backend:8081` | `http://backend:8080` |
| All → MySQL | `mysql:3306` | `mysql:3306` |

### 环境变量（关键配置）

**开发环境（`.env.dev`）**
```bash
MYSQL_PORT=3307
MYSQL_DATABASE=soulmate_db_dev
MYSQL_ROOT_PASSWORD=dev123456
BACKEND_PORT=8081
FRONTEND_PORT=3000
RAG_SERVICE_PORT=8000
SPRING_PROFILES_ACTIVE=dev
DEBUG=true
ENABLE_SWAGGER=true
```

**生产环境（`.env.prod`）**
```bash
MYSQL_PORT=3306
MYSQL_DATABASE=soulmate_db
MYSQL_ROOT_PASSWORD=change_this_in_production  # ⚠️ 必须修改
BACKEND_PORT=8080
FRONTEND_PORT=80
RAG_SERVICE_PORT=8001
SPRING_PROFILES_ACTIVE=prod
DEBUG=false
ENABLE_SWAGGER=false
JWT_SECRET=change_this_jwt_secret_in_production  # ⚠️ 必须修改
```

---

## 🐍 RAG 服务本地启动

如果需要本地启动 RAG 服务（不使用 Docker）：

```powershell
cd ../rag_service
.\venv\Scripts\Activate.ps1  # Windows
# source venv/bin/activate    # Linux/macOS

# 复制并编辑环境变量文件
copy env.dev.example .env.dev
# 修改 .env.dev：
#   DB_HOST=localhost        # 本地启动使用 localhost（Docker 使用 mysql）
#   DB_PORT=3307             # 开发环境端口
#   DB_PASSWORD=dev123456
#   SERVICE_PORT=8000

# 启动服务
$env:ENV="dev"  # Windows
# export ENV=dev  # Linux/macOS
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

**重要提示：**
- Docker 方式：`DB_HOST=mysql`
- 本地方式：`DB_HOST=localhost`
- API Key 从数据库读取，无需环境变量配置

---

## 📋 常用命令

**Linux/macOS：**
```bash
make dev        # 启动开发环境
make prod       # 启动生产环境
make stop-dev   # 停止开发环境
make stop-prod  # 停止生产环境
make logs-dev   # 查看开发环境日志
```

**Windows：**
```cmd
deploy.dev.bat      # 启动开发环境
deploy.prod.bat    # 启动生产环境
docker compose -f docker-compose-dev.yml --env-file .env.dev down  # 停止
docker compose -f docker-compose-dev.yml --env-file .env.dev logs -f  # 日志
```

---

## 📦 前端构建依赖说明

### Terser - 代码压缩工具

**作用：**
- `terser` 是 JavaScript 代码压缩和混淆工具，用于生产环境构建
- Vite v3+ 将 `terser` 设为可选依赖，需要手动安装
- 在生产构建时（`npm run build`）自动压缩 JS 代码，减小文件体积（通常减少 30-70%）

**安装方式：**

如果构建时出现 `terser not found` 错误，需要手动安装：

```bash
cd frontend
npm install terser --save-dev
```

**注意：**
- Docker 构建时会自动安装（通过 `npm install`）
- 本地开发构建时如遇到错误，需要手动安装
- 仅在生产构建时需要，开发模式（`npm run dev`）不需要

---

## 🔧 故障排查

1. 检查容器状态：`docker ps`
2. 查看服务日志：`docker logs <容器名>`
   - 开发环境：`soulmate-dev-backend`、`soulmate-dev-mysql` 等
   - 生产环境：`soulmate-backend`、`soulmate-mysql` 等
3. RAG 服务问题：
   - 检查数据库容器是否启动
   - Docker 方式使用 `DB_HOST=mysql`，本地方式使用 `DB_HOST=localhost`
   - 检查端口是否被占用（开发 8000，生产 8001）
4. 前端构建问题：
   - 如遇到 `terser not found` 错误，需要在 `frontend` 目录执行 `npm install terser --save-dev`
   - 检查 Node.js 版本（推荐 18+）
   - Docker 构建时会自动安装所有依赖，本地构建需要手动安装

---

## 📝 数据库访问

**phpMyAdmin：**
- 开发环境：http://localhost:8082（用户：root，密码：dev123456）
- 生产环境：http://localhost:8083（密码见 `.env.prod`）

**MySQL 命令行：**
```bash
# 开发环境
docker exec -it soulmate-dev-mysql mysql -u root -pdev123456

# 生产环境
docker exec -it soulmate-mysql mysql -u root -p<你的密码>
```

---

## 🔐 默认账号

- **用户名**：`admin`
- **密码**：`admin123`

---

## 📚 更多信息

- [数据库设计](../docs/02数据库设计.md)
- [API 接口设计](../docs/05API接口设计.md)
- [开发指南](../docs/06开发指南.md)
