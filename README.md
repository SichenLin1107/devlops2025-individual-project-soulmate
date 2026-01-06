<h1 align="center">🌸 SoulMate 心伴</h1>

<p align="center">
  <strong>心理陪伴智能体开发与使用平台</strong>
</p>

<p align="center">
  <a href="#快速开始">快速开始</a> •
  <a href="#功能特性">功能特性</a> •
  <a href="#技术架构">技术架构</a> •
  <a href="#部署指南">部署指南</a> •
  <a href="#开发文档">开发文档</a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Vue-3.x-4FC08D?logo=vue.js" alt="Vue 3" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?logo=spring-boot" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/Python-3.10+-3776AB?logo=python" alt="Python" />
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql" alt="MySQL" />
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker" alt="Docker" />
</p>

---

## 📖 目录

- [项目简介](#项目简介)
- [功能特性](#功能特性)
- [技术架构](#技术架构)
- [快速开始](#快速开始)
- [部署指南](#部署指南)
- [环境配置](#环境配置)
- [测试账号](#测试账号)
- [功能模块](#功能模块)
- [API 概览](#api-概览)
- [数据模型](#数据模型)
- [核心技术实现](#核心技术实现)
- [项目结构](#项目结构)
- [常见问题](#常见问题)
- [开发指南](#开发指南)
- [更新日志](#更新日志)

---

## 项目简介

**SoulMate 心伴** 是一个面向心灵陪伴场景的 AI 智能体开发与使用平台。平台集成了智能体管理、知识库管理、工作流编排、LLM 模型管理、敏感词风控等核心功能，为用户提供专业、安全、温暖的心理陪伴体验。

### ✨ 核心亮点

| 特性 | 说明 |
|------|------|
| 🤖 **智能体引擎** | 支持自定义人设、多模型切换、参数调优 |
| 📚 **RAG 知识增强** | 基于向量检索的知识库问答，提升回复专业性 |
| 🔄 **可视化工作流** | 拖拽式编排对话流程，支持条件分支与危机干预 |
| 🛡️ **安全风控** | 敏感词检测、危机干预机制、API Key 加密存储 |
| 🎨 **现代化 UI** | Vue 3 + Element Plus + Tailwind CSS，响应式设计 |
| 🐳 **容器化部署** | Docker Compose 一键启动，开发/生产环境分离 |

### 📦 预置资源

- **8 个心理陪伴智能体**：心声树洞、情感解语、深夜心灯、职场加油站等
- **5 个专业知识库**：心理学基础、情绪识别、情感关系、睡眠焦虑、职场心理
- **2 个示例工作流**：简单安全陪伴、情绪识别陪伴

---

## 功能特性

### 用户端功能

| 功能模块 | 说明 |
|---------|------|
| 🏠 **首页** | 平台介绍、服务特色展示、快速入口 |
| 🎭 **智能体广场** | 卡片式展示、关键词搜索、热度/最新排序、分页浏览 |
| 💬 **智能对话** | 三栏布局、Markdown 渲染、知识库引用标识、会话管理 |
| 📜 **历史记录** | 会话检索、批量管理、详情查看 |
| 👤 **个人中心** | 资料编辑、头像上传、密码修改 |

### 管理端功能

| 功能模块 | 说明 |
|---------|------|
| 🤖 **智能体管理** | 创建/编辑智能体、资源配置、测试对话、上下架管理 |
| 📚 **知识库管理** | 文档上传、异步处理、检索测试、状态追踪 |
| 🔄 **工作流管理** | Vue Flow 可视化编辑器、节点拖拽、连线配置、验证发布 |
| 🧠 **模型管理** | 提供商/模型配置、API Key 管理、对话测试 |
| 🚫 **敏感词管理** | 分类管理、动作配置、批量操作 |
| 👥 **用户管理** | 用户 CRUD、角色分配、状态管理（仅超级管理员） |

---

## 技术架构

### 系统架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                         用户端 / 管理端                           │
│                    Vue 3 + Element Plus + Tailwind               │
└───────────────────────────────┬─────────────────────────────────┘
                                │ HTTP/REST
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                         Nginx (生产环境)                          │
│                      静态资源服务 + 反向代理                        │
└───────────────────────────────┬─────────────────────────────────┘
                                │
            ┌───────────────────┴───────────────────┐
            ▼                                       ▼
┌───────────────────────┐               ┌───────────────────────┐
│     后端 API 服务      │               │     RAG 服务          │
│  Spring Boot 3.2      │◄─────────────►│  FastAPI + ChromaDB   │
│  Java 17 + MyBatis    │   HTTP        │  Python 3.10+         │
└───────────┬───────────┘               └───────────┬───────────┘
            │                                       │
            └───────────────────┬───────────────────┘
                                ▼
                    ┌───────────────────────┐
                    │      MySQL 8.0        │
                    │   用户/智能体/会话等    │
                    └───────────────────────┘
```

### 技术栈详情

| 层级 | 技术选型 | 版本 |
|------|---------|------|
| **前端** | Vue 3 + Vue Router + Pinia | 3.4+ |
| | Element Plus | 2.5+ |
| | Tailwind CSS | 3.4+ |
| | Vue Flow（工作流编辑器） | 1.x |
| | Vite（构建工具） | 5.x |
| **后端** | Spring Boot | 3.2 |
| | Java | 17 |
| | MyBatis | 3.5+ |
| | JWT (jjwt) | 0.12+ |
| **RAG 服务** | FastAPI | 0.109+ |
| | ChromaDB | 0.4+ |
| | 智谱 AI Embedding | embedding-2 |
| **数据库** | MySQL | 8.0 |
| **部署** | Docker + Docker Compose | 2.0+ |

### LLM 模型支持

| 提供商 | 支持模型 | 类型 |
|-------|---------|------|
| DeepSeek | deepseek-chat | Chat |
| 智谱 AI | glm-4-flash, glm-4 | Chat |
| | embedding-2 | Embedding |
| 通义千问 | qwen-turbo, qwen-plus | Chat |
| | text-embedding-v2 | Embedding |
| 豆包 | doubao-pro-32k | Chat |

---

## 快速开始

### 前置要求

- **Docker Desktop** 或 **Docker Engine + Docker Compose v2.0+**
- **可用端口**：
  - 开发模式：3000, 8081, 8000, 3307, 8082
  - 生产模式：80, 8080, 8001, 3306, 8083
- **系统资源**：建议 4GB+ 内存

### 一键启动（开发模式）

```bash
# 1. 克隆项目
git clone <repository-url>
cd soulmate

# 2. 进入部署目录
cd deploy_soulmate

# 3. 创建环境配置文件
# Windows:
copy env.dev.example .env.dev
# Linux/macOS:
cp env.dev.example .env.dev

# 4. 启动所有服务
# Windows:
docker compose -f docker-compose-dev.yml --env-file .env.dev up -d --build
# Linux/macOS:
make dev

# 5. 等待服务就绪 
docker compose -f docker-compose-dev.yml --env-file .env.dev logs -f
```

### 访问服务

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端页面 | http://localhost:3000 | 主应用入口 |
| 后端 API | http://localhost:8081 | RESTful API |
| Swagger 文档 | http://localhost:8081/swagger-ui.html | API 文档 |
| RAG 服务文档 | http://localhost:8000/docs | FastAPI 文档 |
| phpMyAdmin | http://localhost:8082 | 数据库管理 |

### 验证安装

```bash
# 检查服务状态
docker compose -f docker-compose-dev.yml --env-file .env.dev ps

# 健康检查
curl http://localhost:8081/api/v1/health
curl http://localhost:8000/api/rag/health
```

---

## 部署指南

### 开发环境

开发模式支持热重载、Swagger 文档、详细日志，适合功能开发和调试。

```bash
cd deploy_soulmate

# Windows
copy env.dev.example .env.dev
docker compose -f docker-compose-dev.yml --env-file .env.dev up -d --build

# Linux/macOS
cp env.dev.example .env.dev
make dev
```

### 生产环境

生产模式关闭调试功能，优化性能和安全性。

```bash
cd deploy_soulmate

# Windows
copy env.prod.example .env.prod
# ⚠️ 编辑 .env.prod，修改以下配置：
# - MYSQL_ROOT_PASSWORD: 设置强密码
# - JWT_SECRET: 设置 32 位以上随机字符串
docker compose -f docker-compose-prod.yml --env-file .env.prod up -d --build

# Linux/macOS
cp env.prod.example .env.prod
# 编辑 .env.prod 修改密码和密钥
make prod
```

### 停止服务

```bash
# 开发环境
# Windows:
docker compose -f docker-compose-dev.yml --env-file .env.dev down
# Linux/macOS:
make stop-dev

# 生产环境
# Windows:
docker compose -f docker-compose-prod.yml --env-file .env.prod down
# Linux/macOS:
make stop-prod

# 完全清理（包括数据卷）
docker compose -f docker-compose-dev.yml --env-file .env.dev down -v
```

---

## 环境配置

### 环境变量说明

#### 通用配置

| 变量名 | 说明 | 开发默认值 | 生产建议值 |
|-------|------|-----------|-----------|
| `PROJECT_NAME` | 项目名称前缀 | soulmate-dev | soulmate |
| `TZ` | 时区 | Asia/Shanghai | Asia/Shanghai |

#### 数据库配置

| 变量名 | 说明 | 开发默认值 | 生产建议值 |
|-------|------|-----------|-----------|
| `MYSQL_PORT` | MySQL 端口 | 3307 | 3306 |
| `MYSQL_DATABASE` | 数据库名 | soulmate_db_dev | soulmate_db |
| `MYSQL_ROOT_PASSWORD` | 数据库密码 | dev123456 | **强密码** |

#### 后端配置

| 变量名 | 说明 | 开发默认值 | 生产建议值 |
|-------|------|-----------|-----------|
| `SPRING_PROFILES_ACTIVE` | Spring 配置文件 | dev | prod |
| `DEBUG` | 调试模式 | true | false |
| `LOG_LEVEL` | 日志级别 | debug | info |
| `ENABLE_SWAGGER` | Swagger 文档 | true | false |
| `JWT_SECRET` | JWT 密钥 | dev_xxx | **32位以上随机字符串** |

#### 服务端口配置

| 变量名 | 说明 | 开发默认值 | 生产默认值 |
|-------|------|-----------|-----------|
| `FRONTEND_PORT` | 前端端口 | 3000 | 80 |
| `BACKEND_PORT` | 后端端口 | 8081 | 8080 |
| `RAG_SERVICE_PORT` | RAG 服务端口 | 8000 | 8001 |
| `PHPMYADMIN_PORT` | phpMyAdmin 端口 | 8082 | 8083 |

### 配置 LLM API Key

系统已预置以下测试 API Key，可直接用于功能测试：

| 提供商 | API Key | 说明 |
|-------|---------|------|
| DeepSeek | `sk-192531ad13d14154bd497525dc0264de` | 默认模型 |
| 智谱 AI | `bb4ea46c4b254cd2b7cafecb382dc8c9.JJnKOPv4FZHdCbKE` | 用于 Embedding |
| 通义千问 | `sk-a0c14831ae154be8802c1c5bf02a803a` | 备用模型 |

> **提示**：以上 API Key 已写入数据库初始化脚本，部署后可直接使用。如需更换，可通过管理后台修改。

**手动配置步骤**（可选）：
1. 登录管理后台（使用 admin 账号）
2. 进入「模型管理」→「提供商管理」
3. 选择提供商（如 DeepSeek、智谱 AI）
4. 配置 API Key（支持 AES 加密存储）
5. 测试连接，确保配置正确

---

## 测试账号

系统预置以下测试账号：

| 角色 | 用户名 | 密码 | 权限说明 |
|-----|--------|------|---------|
| 超级管理员 | `superadmin` | `admin123` | 全部功能 + 用户管理 |
| 管理员 | `admin` | `admin123` | 智能体/知识库/工作流/模型/敏感词管理 |
| 普通用户 | `zhangsan_996` | `user123` | 对话、历史记录、个人中心 |

> **安全提示**：生产环境部署后请立即修改默认密码！

---

## 功能模块

### 用户端

#### 智能体广场 (`/agents`)

- 卡片式展示已上架智能体（头像、名称、简介、标签、热度）
- 关键词搜索（支持名称、描述、标签，300ms 防抖）
- 排序方式：热度优先（默认）、最新发布
- 分页展示（12/24/48 条/页）

#### 智能对话 (`/agents/:id`)

- **三栏布局**：左侧会话列表、中间消息区、右侧智能体信息
- **消息功能**：发送/接收消息、Markdown 渲染、知识库引用标识
- **会话管理**：新建、重命名、置顶、删除、清空消息
- **智能特性**：
  - 自动携带最近 10 条消息作为上下文
  - 首次对话后自动生成会话标题
  - RAG 知识库检索增强回复质量
  - 工作流执行（安全检测、危机干预）

### 管理端

#### 智能体管理 (`/admin/agents`)

**详情编辑页三栏布局**：
- **左栏**：基本信息（名称、头像、简介、标签、系统提示词、问候语）
- **中栏**：资源配置（模型选择、知识库绑定、工作流关联、参数覆盖）
- **右栏**：测试对话与状态管理

#### 知识库管理 (`/admin/knowledge-bases`)

- 文档上传（支持 TXT/MD 格式，最大 50MB）
- 异步处理状态追踪（pending → processing → completed/failed）
- 失败重试机制（最多 3 次）
- 检索测试：输入查询返回 Top-K 相似片段及相似度分数

#### 工作流管理 (`/admin/workflows`)

**可视化编辑器**（Vue Flow）：
- 左侧节点工具箱（按分类展示、支持搜索）
- 中间画布（拖拽、连线、网格对齐、缩放 20%-200%）
- 右侧节点配置面板
- 工具栏：撤销/重做（Ctrl+Z/Y）、自动布局、验证、保存发布

**支持的节点类型**：

| 节点 | 类型 | 功能 |
|-----|------|------|
| 开始 | `start` | 工作流入口，接收用户输入 |
| 文本处理 | `text_process` | 文本清洗与预处理 |
| 安全检测 | `safety_check` | 敏感词检测，分流 safe/crisis |
| 危机干预 | `crisis_intervention` | 推送心理援助热线 |
| 知识检索 | `rag_retrieval` | 向量相似度检索 |
| LLM 处理 | `llm_process` | 调用大模型生成回复 |
| 结束 | `end` | 工作流出口 |

#### 敏感词管理 (`/admin/sensitive-words`)

| 分类 | 说明 | 推荐动作 |
|-----|------|---------|
| `general` | 一般敏感词 | replace / warn |
| `crisis` | 危机干预词 | intervention |
| `prohibited` | 绝对禁止词 | block |

| 动作 | 说明 |
|-----|------|
| `block` | 直接拦截，返回提示 |
| `warn` | 记录警告，继续处理 |
| `replace` | 用替换文本替代 |
| `intervention` | 触发危机干预，推送心理援助热线（400-161-9995） |

---

## API 概览

### 接口分类

| 模块 | 基础路径 | 说明 |
|------|---------|------|
| 认证 | `/api/v1/auth` | 登录、注册、登出 |
| 个人中心 | `/api/v1/profile` | 个人信息管理 |
| 智能体 | `/api/v1/agents` | 智能体 CRUD |
| 知识库 | `/api/v1/knowledge-bases` | 知识库管理 |
| 工作流 | `/api/v1/workflows` | 工作流管理 |
| LLM 模型 | `/api/v1/llm` | 提供商/模型管理 |
| 敏感词 | `/api/v1/sensitive-words` | 敏感词管理 |
| 会话 | `/api/v1/sessions` | 会话管理 |
| 消息 | `/api/v1/sessions/{id}/messages` | 消息收发 |
| 用户管理 | `/api/v1/users` | 用户 CRUD（SuperAdmin） |
| RAG 服务 | `/api/rag` | 向量化、检索 |

### 统一响应格式

```json
{
  "code": 0,
  "message": "操作成功",
  "data": { },
  "timestamp": 1704067200000
}
```

### 权限说明

| 接口 | 公开 | 登录 | Admin | SuperAdmin |
|-----|:----:|:----:|:-----:|:----------:|
| 认证接口 | ✅ | - | - | - |
| 智能体列表 | ✅ | - | - | - |
| 智能体管理 | - | ✅ | ✅ | ✅ |
| 知识库/工作流/模型/敏感词 | - | ✅ | ✅ | ✅ |
| 会话/消息 | - | ✅ | - | - |
| 用户管理 | - | ✅ | - | ✅ |

> 详细 API 文档请参阅 [docs/03API接口设计.md](docs/03API接口设计.md)

---

## 数据模型

### 数据库 ER 图

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  sys_user   │     │  ai_agent   │     │ ai_workflow │
├─────────────┤     ├─────────────┤     ├─────────────┤
│ id          │◄────│ created_by  │     │ id          │
│ username    │     │ model_id    │────►│ created_by  │
│ password    │     │ workflow_id │────►│ nodes_config│
│ role        │     │ ...         │     │ ...         │
└─────────────┘     └──────┬──────┘     └─────────────┘
      │                    │
      │                    │ ai_agent_knowledge
      │                    ▼
      │             ┌─────────────┐
      │             │ai_knowledge │
      │             │    _base    │
      │             ├─────────────┤
      │             │ id          │
      │             │ name        │
      │             │ ...         │
      │             └──────┬──────┘
      │                    │
      │                    ▼
      │             ┌─────────────┐
      │             │ai_knowledge │
      │             │  _document  │
      │             └──────┬──────┘
      │                    │
      │                    ▼
      │             ┌─────────────┐
      │             │ai_knowledge │
      │             │  _segment   │
      │             └─────────────┘
      │
      ▼
┌─────────────┐     ┌─────────────┐
│chat_session │────►│chat_message │
└─────────────┘     └─────────────┘
```

### 核心数据表

| 表名 | 说明 |
|------|------|
| `sys_user` | 用户表 |
| `sys_sensitive_word` | 敏感词表 |
| `llm_provider` | LLM 提供商表 |
| `llm_model` | LLM 模型表 |
| `ai_knowledge_base` | 知识库表 |
| `ai_knowledge_document` | 知识库文档表 |
| `ai_knowledge_segment` | 知识切片表 |
| `ai_workflow` | 工作流表 |
| `ai_agent` | 智能体表 |
| `ai_agent_knowledge` | 智能体-知识库关联表 |
| `chat_session` | 会话表 |
| `chat_message` | 消息表 |
| `ai_workflow_execution` | 工作流执行历史表 |

> 详细表结构请参阅 [docs/02数据库设计.md](docs/02数据库设计.md)

---

## 核心技术实现

### 用户认证与权限

- **JWT Token**：24 小时有效期，HS256 签名
- **密码安全**：BCrypt 加密存储
- **接口鉴权**：`@RequireRole` 自定义注解
- **角色体系**：User → Admin → SuperAdmin

### RAG 知识检索

```
文档上传 → 文本切片(~500字) → 向量化(embedding-2) → ChromaDB存储
     ↓
用户查询 → 向量化 → 相似度检索 → Top-K结果 → 注入系统提示词
```

- 切片策略：约 500 字/片，支持重叠
- 异步处理：文档上传后异步向量化
- 重试机制：失败最多重试 3 次
- 向量数据库：ChromaDB 持久化存储

### 工作流引擎

- **拓扑排序**：确定节点执行顺序
- **条件分支**：安全检测节点分流 safe/crisis
- **数据流转**：节点间上下文传递
- **可视化**：Vue Flow 拖拽式编辑

### 安全风控

- **敏感词检测**：实时匹配检测
- **危机干预**：自动推送心理援助热线
- **API Key 保护**：AES 加密存储

### LLM 调用

- **统一接口**：OpenAI 兼容格式
- **多厂商支持**：DeepSeek、智谱、通义、豆包
- **参数可配**：temperature、max_tokens、top_p 等
- **智能体覆盖**：支持智能体级参数覆盖

---

## 项目结构

```
soulmate/
├── backend/                    # 后端服务 (Spring Boot)
│   ├── src/main/java/com/soulmate/
│   │   ├── controller/         # 控制器层
│   │   ├── service/            # 服务层
│   │   ├── mapper/             # MyBatis Mapper
│   │   ├── entity/             # 实体类
│   │   ├── dto/                # 数据传输对象
│   │   ├── config/             # 配置类
│   │   ├── security/           # 安全相关
│   │   ├── annotation/         # 自定义注解
│   │   └── common/             # 公共模块
│   ├── src/main/resources/
│   │   ├── application.yml     # 主配置
│   │   ├── application-dev.yml # 开发配置
│   │   └── application-prod.yml# 生产配置
│   ├── Dockerfile.dev
│   └── Dockerfile.prod
│
├── frontend/                   # 前端应用 (Vue 3)
│   ├── src/
│   │   ├── api/                # API 封装
│   │   ├── components/         # 公共组件
│   │   ├── views/              # 页面视图
│   │   │   ├── admin/          # 管理端页面
│   │   │   └── *.vue           # 用户端页面
│   │   ├── router/             # 路由配置
│   │   ├── stores/             # Pinia 状态
│   │   ├── styles/             # 样式文件
│   │   └── utils/              # 工具函数
│   ├── Dockerfile.dev
│   └── Dockerfile.prod
│
├── rag_service/                # RAG 服务 (FastAPI)
│   ├── app/
│   │   ├── api/                # API 路由
│   │   ├── services/           # 服务层
│   │   ├── config.py           # 配置
│   │   └── main.py             # 入口
│   ├── data/chroma/            # ChromaDB 数据
│   ├── requirements.txt
│   ├── Dockerfile.dev
│   └── Dockerfile.prod
│
├── deploy_soulmate/            # 部署配置
│   ├── database/
│   │   ├── init/               # 数据库初始化脚本
│   │   └── 示例文档/            # 知识库示例文档
│   ├── docker-compose-dev.yml
│   ├── docker-compose-prod.yml
│   ├── env.dev.example
│   ├── env.prod.example
│   └── Makefile
│
├── docs/                       # 项目文档
│   ├── 01用户需求分析.md
│   ├── 02数据库设计.md
│   ├── 03API接口设计.md
│   └── 04开发指南.md
│
└── README.md
```

### 代码统计

| 模块 | 文件数 | 主要功能 |
|-----|--------|---------|
| **后端** | ~80 | |
| - 用户认证 | 12 | JWT 鉴权、角色权限 |
| - LLM 模型 | 10 | 提供商/模型管理 |
| - 知识库 | 12 | 文档处理、切片管理 |
| - 工作流 | 15 | 执行引擎、节点执行器 |
| - 智能体 | 10 | 资源绑定、状态管理 |
| - 对话 | 8 | 会话/消息管理 |
| **前端** | ~30 | |
| - 用户端 | 7 | 首页、广场、对话等 |
| - 管理端 | 13 | 各管理模块 |
| - 工作流编辑器 | 8 | Vue Flow 画布、节点 |
| **RAG 服务** | 6 | 向量化、检索 |

---

## 常见问题

### Q1: 服务启动失败

**检查步骤**：

```bash
# 1. 检查 Docker 状态
docker ps

# 2. 检查端口占用
# Windows:
netstat -an | findstr "3000 8081 8000 3307"
# Linux/macOS:
lsof -i :3000,:8081,:8000,:3307

# 3. 查看服务日志
docker logs soulmate-dev-mysql
docker logs soulmate-dev-backend
docker logs soulmate-dev-rag-service
docker logs soulmate-dev-frontend
```

**常见原因**：
- 端口被占用：修改 `.env.dev` 中的端口配置
- 内存不足：Docker 至少需要 4GB 内存
- 网络问题：检查 Docker 网络配置

### Q2: 对话无响应

1. 确认已配置有效的 LLM API Key
   - 管理后台 → 模型管理 → 提供商管理
2. 检查后端日志：`docker logs soulmate-dev-backend`
3. 确认智能体已关联有效的模型
4. 测试模型连接：使用模型管理中的「测试对话」功能

### Q3: 知识库检索无结果

1. 确认文档处理状态为 `completed`
2. 检查 RAG 服务日志：`docker logs soulmate-dev-rag-service`
3. 确认知识库已绑定到智能体
4. 使用知识库管理中的「检索测试」功能验证

### Q4: 工作流执行失败

1. 检查工作流是否已发布（status = published）
2. 验证节点配置是否完整（所有必填项）
3. 查看工作流执行历史，分析失败原因

### Q5: 数据库连接失败

```bash
# 检查 MySQL 容器状态
docker logs soulmate-dev-mysql

# 手动连接测试
docker exec -it soulmate-dev-mysql mysql -uroot -p
# 输入密码：dev123456
```

### Q6: 查看完整日志

```bash
# 后端日志（实时）
docker logs -f soulmate-dev-backend

# RAG 服务日志
docker logs -f soulmate-dev-rag-service

# 全部服务日志
docker compose -f docker-compose-dev.yml --env-file .env.dev logs -f
```

---

## 开发指南

### 本地开发环境

#### 后端开发

```bash
cd backend

# 安装依赖
./mvnw clean install -DskipTests

# 启动开发服务器
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 或使用 IDE 运行 SoulmateApplication
```

#### 前端开发

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build
```

#### RAG 服务开发

```bash
cd rag_service

# 创建虚拟环境
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# 安装依赖
pip install -r requirements.txt

# 启动开发服务器
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

### 代码规范

- **后端**：遵循阿里巴巴 Java 开发手册
- **前端**：ESLint + Prettier，Vue 3 组合式 API
- **提交信息**：遵循 Conventional Commits 规范

```
feat: 添加用户头像上传功能
fix: 修复会话标题更新失败的问题
docs: 更新 API 文档
refactor: 重构工作流执行引擎
```

### 测试

```bash
# 后端单元测试
cd backend
./mvnw test

# 前端测试
cd frontend
npm run test
```

---

## 更新日志

### v1.0.0 (2026-01-06)

**首个正式版本发布**

#### 功能特性
- ✅ 用户认证与权限管理（JWT + BCrypt）
- ✅ 智能体管理（创建、编辑、上下架、测试对话）
- ✅ 知识库管理（文档上传、异步切片、向量检索）
- ✅ 工作流可视化编辑器（Vue Flow）
- ✅ LLM 模型管理（多厂商支持）
- ✅ 敏感词风控（检测、干预、替换）
- ✅ 智能对话（上下文、知识增强、工作流执行）
- ✅ Docker Compose 一键部署

#### 预置数据
- 8 个心理陪伴智能体
- 5 个专业知识库
- 2 个示例工作流
- 敏感词库

---

## 项目文档

| 文档 | 说明 |
|-----|------|
| [用户需求分析](docs/01用户需求分析.md) | 完整功能需求说明 |
| [数据库设计](docs/02数据库设计.md) | 数据库表结构设计 |
| [API 接口设计](docs/03API接口设计.md) | RESTful API 接口文档 |
| [开发指南](docs/04开发指南.md) | 开发阶段与任务清单 |
| [部署指南](deploy_soulmate/README.md) | 详细部署说明 |

---

## 许可证

本项目仅供学习交流使用。

---

## 致谢

- [Vue.js](https://vuejs.org/) - 渐进式 JavaScript 框架
- [Spring Boot](https://spring.io/projects/spring-boot) - Java 快速开发框架
- [FastAPI](https://fastapi.tiangolo.com/) - 高性能 Python Web 框架
- [Element Plus](https://element-plus.org/) - Vue 3 组件库
- [Vue Flow](https://vueflow.dev/) - Vue 3 工作流编辑器
- [ChromaDB](https://www.trychroma.com/) - 开源向量数据库
- [DeepSeek](https://www.deepseek.com/)、[智谱 AI](https://www.zhipuai.cn/)、[通义千问](https://tongyi.aliyun.com/) - LLM 模型服务

---

<p align="center">
  <strong>🌸 SoulMate 心伴 - 用心陪伴每一刻</strong>
</p>

<p align="center">
  <sub>文档版本：v1.0 | 最后更新：2026-01-06</sub>
</p>
