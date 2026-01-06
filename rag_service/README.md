# RAG Service

RAG（检索增强生成）服务，提供文本向量化和向量检索功能。

## 环境配置

### 1. 创建环境变量文件

根据你的环境选择对应的配置文件：

**开发环境**：
```bash
cd rag_service
cp env.dev.example .env.dev
# 编辑 .env.dev，修改数据库密码等配置
```

**生产环境**：
```bash
cd rag_service
cp env.prod.example .env.prod
# 编辑 .env.prod，修改数据库密码等配置（必须使用强密码）
```

### 2. 配置说明

#### 开发环境 (.env.dev)

```env
# 服务配置
SERVICE_HOST=0.0.0.0
SERVICE_PORT=8000
DEBUG=true

# 数据库配置（用于读取智谱AI API key）
DB_HOST=localhost
DB_PORT=3307          # 开发环境MySQL端口
DB_USER=root
DB_PASSWORD=dev123456  # 开发环境密码
DB_NAME=soulmate_db_dev  # 开发环境数据库名

# 智谱AI配置（可选，优先从数据库读取）
EMBEDDING_MODEL=embedding-2

# ChromaDB配置
CHROMA_PERSIST_PATH=./data/chroma
```

#### 生产环境 (.env.prod)

```env
# 服务配置
SERVICE_HOST=0.0.0.0
SERVICE_PORT=8001
DEBUG=false

# 数据库配置（用于读取智谱AI API key）
DB_HOST=localhost
DB_PORT=3306          # 生产环境MySQL端口
DB_USER=root
DB_PASSWORD=your_strong_password  # 请修改为强密码
DB_NAME=soulmate_db  # 生产环境数据库名

# 智谱AI配置（可选，优先从数据库读取）
EMBEDDING_MODEL=embedding-2

# ChromaDB配置
CHROMA_PERSIST_PATH=./data/chroma
```

### 3. 启动服务

#### 方式一：使用环境变量指定配置文件

**开发环境**：
```bash
# Windows PowerShell
$env:ENV="dev"
python app/main.py

# 或使用 uvicorn
$env:ENV="dev"
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

**生产环境**：
```bash
# Windows PowerShell
$env:ENV="prod"
python app/main.py

# 或使用 uvicorn
$env:ENV="prod"
uvicorn app.main:app --host 0.0.0.0 --port 8001
```

#### 方式二：直接使用 .env 文件（推荐）

更简单的方式是直接复制对应的配置文件为 `.env`：

**开发环境**：
```bash
cp env.dev.example .env
# 编辑 .env，修改配置
python app/main.py
```

**生产环境**：
```bash
cp env.prod.example .env
# 编辑 .env，修改配置
python app/main.py
```

### 4. 重要说明

1. **API Key 配置**：
   - API key 优先从数据库的 `llm_provider` 表中读取（provider_id='prv_zhipu'）
   - 如果数据库读取失败，会使用环境变量中的 `ZHIPU_API_KEY` 作为后备
   - 建议在数据库管理后台配置 API key，这样更安全且便于管理

2. **数据库连接**：
   - 确保数据库服务已启动
   - 开发环境默认端口：3307，数据库名：soulmate_db_dev
   - 生产环境默认端口：3306，数据库名：soulmate_db

3. **端口配置**：
   - 开发环境默认端口：8000
   - 生产环境默认端口：8001
   - 如果端口被占用，可在对应的 `.env.dev` 或 `.env.prod` 文件中修改 `SERVICE_PORT`

## 测试接口

服务启动后，可以访问（根据环境使用对应端口）：

**开发环境**（端口 8000）：
1. **健康检查**：
   ```bash
   GET http://localhost:8000/api/rag/health
   ```

2. **文本向量化**（单个）：
   ```bash
   POST http://localhost:8000/api/rag/embed
   Content-Type: application/json
   
   {
     "text": "这是要向量化的文本"
   }
   ```

3. **文本向量化**（批量）：
   ```bash
   POST http://localhost:8000/api/rag/embed
   Content-Type: application/json
   
   {
     "texts": ["文本1", "文本2", "文本3"]
   }
   ```

**生产环境**（端口 8001）：
将上述 URL 中的 `8000` 替换为 `8001` 即可。

## API 文档

服务启动后，访问对应的 Swagger API 文档：
- **开发环境**：`http://localhost:8000/docs`
- **生产环境**：`http://localhost:8001/docs`

