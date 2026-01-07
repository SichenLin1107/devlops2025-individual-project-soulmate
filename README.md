<h1 align="center">🌸 SoulMate 心伴</h1>

<p align="center">
  <strong>心理陪伴智能体开发与使用平台</strong>
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

- [一键启动](#-一键启动)
- [部署指南](#-部署指南开发环境--生产环境)
- [项目简介](#-项目简介)
- [功能特性](#-功能特性详解)
- [技术架构](#-系统架构)
- [核心技术实现](#-核心技术实现)
- [数据库设计](#️-数据库设计)
- [项目结构](#-项目结构)
- [常见问题](#-常见问题)
- [项目文档](#-项目文档)

---

## 🚀 一键启动

> **前置要求**：已安装 [Docker Desktop](https://www.docker.com/products/docker-desktop/)（Windows/Mac）或 Docker Engine + Docker Compose v2.0+
>
> **系统资源**：建议 4GB+ 内存

### Windows 用户（开发环境）

```powershell
# 1. 进入部署目录
cd deploy_soulmate

# 2. 创建配置文件
copy env.dev.example .env.dev

# 3. 一键启动所有服务（首次启动约需5-10分钟）
docker compose -f docker-compose-dev.yml --env-file .env.dev up -d --build
```

### Linux / macOS 用户（开发环境）

```bash
cd deploy_soulmate
cp env.dev.example .env.dev
make dev    # 使用 Makefile 一键启动
```

### ✅ 启动成功后访问（开发环境）

| 服务 | 地址 | 说明 |
|:---:|:---:|:---:|
| **🖥️ 前端页面** | http://localhost:3000 | 主应用入口 |
| **📡 后端 API** | http://localhost:8081 | RESTful API |
| **📚 Swagger 文档** | http://localhost:8081/swagger-ui.html | API 在线文档 |
| **🔍 RAG 服务文档** | http://localhost:8000/docs | FastAPI 文档 |
| **🗄️ phpMyAdmin** | http://localhost:8082 | 数据库管理（root / dev123456） |

### 🔑 测试账号

| 角色 | 用户名 | 密码 | 权限说明 |
|:---:|:---:|:---:|:---|
| 超级管理员 | `superadmin` | `admin123` | 全部功能 + 用户管理 |
| 管理员 | `admin` | `admin123` | 智能体/知识库/工作流/模型/敏感词管理 |
| 普通用户 | `zhangsan_996` | `user123` | 对话、历史记录、个人中心 |

---

## 📦 部署指南（开发环境 / 生产环境）

系统提供 **开发环境** 和 **生产环境** 两套部署配置，区别如下：

### 环境对比

| 配置项 | 开发环境 (dev) | 生产环境 (prod) |
|:---|:---:|:---:|
| **前端端口** | 3000 | 80 |
| **后端端口** | 8081 | 8080 |
| **RAG 服务端口** | 8000 | 8001 |
| **MySQL 端口** | 3307 | 3306 |
| **phpMyAdmin 端口** | 8082 | 8083 |
| **Swagger 文档** | ✅ 开启 | ❌ 关闭 |
| **调试模式** | ✅ 开启 | ❌ 关闭 |
| **日志级别** | debug | info |
| **热重载** | ✅ 支持 | ❌ 不支持 |

---

### 🔧 开发环境部署

适用于**功能开发、调试测试**，支持热重载和详细日志。

#### Windows

```powershell
cd deploy_soulmate

# 创建配置文件
copy env.dev.example .env.dev

# 启动服务
docker compose -f docker-compose-dev.yml --env-file .env.dev up -d --build
```

#### Linux / macOS

```bash
cd deploy_soulmate
cp env.dev.example .env.dev
make dev    # 使用 Makefile 一键启动
```

#### 开发环境访问地址

| 服务 | 地址 |
|:---|:---|
| 前端页面 | http://localhost:3000 |
| 后端 API | http://localhost:8081 |
| Swagger 文档 | http://localhost:8081/swagger-ui.html |
| RAG 服务文档 | http://localhost:8000/docs |
| phpMyAdmin | http://localhost:8082 |
| MySQL | localhost:3307 (root / dev123456) |

#### 停止开发环境

```bash
# Windows
docker compose -f docker-compose-dev.yml --env-file .env.dev down

# Linux / macOS
make stop-dev
```

---

### 🚀 生产环境部署

适用于**正式部署上线**，优化性能和安全性。

#### Windows

```powershell
cd deploy_soulmate

# 创建配置文件
copy env.prod.example .env.prod

# ⚠️ 重要：编辑 .env.prod，修改以下配置
# - MYSQL_ROOT_PASSWORD: 设置强密码（替换 change_this_in_production）
# - JWT_SECRET: 设置 32 位以上随机字符串（替换 change_this_jwt_secret_in_production）

# 启动服务
docker compose -f docker-compose-prod.yml --env-file .env.prod up -d --build
```

#### Linux / macOS

```bash
cd deploy_soulmate
cp env.prod.example .env.prod

# ⚠️ 编辑 .env.prod，修改密码和密钥
vim .env.prod  # 或使用其他编辑器

make prod    # 使用 Makefile 一键启动
```

#### 生产环境访问地址

| 服务 | 地址 |
|:---|:---|
| 前端页面 | http://localhost (80端口) |
| 后端 API | http://localhost:8080 |
| RAG 服务 | http://localhost:8001 |
| phpMyAdmin | http://localhost:8083 |
| MySQL | localhost:3306 |

> ⚠️ **安全提示**：生产环境部署后请立即修改默认测试账号密码！

#### 停止生产环境

```bash
# Windows
docker compose -f docker-compose-prod.yml --env-file .env.prod down

# Linux / macOS
make stop-prod
```

---

### 📋 常用运维命令

#### Linux / macOS（使用 Makefile）

```bash
make dev          # 启动开发环境
make prod         # 启动生产环境
make stop-dev     # 停止开发环境
make stop-prod    # 停止生产环境
make logs-dev     # 查看开发环境日志
make logs-prod    # 查看生产环境日志
make help         # 查看所有可用命令
```

#### Windows / 通用命令

```bash
# ========== 开发环境 ==========
# 查看容器状态
docker compose -f docker-compose-dev.yml --env-file .env.dev ps

# 查看实时日志
docker compose -f docker-compose-dev.yml --env-file .env.dev logs -f

# 查看单个服务日志
docker logs -f soulmate-dev-backend
docker logs -f soulmate-dev-rag-service

# 重启单个服务
docker compose -f docker-compose-dev.yml --env-file .env.dev restart backend

# 完全清理（包括数据卷）
docker compose -f docker-compose-dev.yml --env-file .env.dev down -v

# ========== 生产环境 ==========
# 查看容器状态
docker compose -f docker-compose-prod.yml --env-file .env.prod ps

# 查看实时日志
docker compose -f docker-compose-prod.yml --env-file .env.prod logs -f

# 完全清理
docker compose -f docker-compose-prod.yml --env-file .env.prod down -v
```

### 🔍 健康检查

```bash
# 开发环境
curl http://localhost:8081/api/v1/health
curl http://localhost:8000/api/rag/health

# 生产环境
curl http://localhost:8080/api/v1/health
curl http://localhost:8001/api/rag/health
```

---

## 📖 项目简介

**SoulMate 心伴** 是一个面向心灵陪伴场景的 AI 智能体开发与使用平台。平台集成了智能体管理、知识库管理、工作流编排、LLM 模型管理、敏感词风控等核心功能，为用户提供专业、安全、温暖的心理陪伴体验。

### ✨ 核心亮点

| 特性 | 说明 |
|:---|:---|
| 🤖 **智能体引擎** | 支持自定义人设、多模型切换、参数调优 |
| 📚 **RAG 知识增强** | 基于向量检索的知识库问答，提升回复专业性 |
| 🔄 **可视化工作流** | Vue Flow 拖拽式编排对话流程，支持条件分支与危机干预 |
| 🛡️ **安全风控** | 敏感词检测、危机干预机制、API Key 加密存储 |
| 🎨 **现代化 UI** | Vue 3 + Element Plus + Tailwind CSS，响应式设计 |
| 🐳 **容器化部署** | Docker Compose 一键启动，开发/生产环境分离 |

### 📦 预置资源（开箱即用）

系统已预置丰富的示例数据，部署后可直接体验完整功能：

| 类型 | 内容 |
|:---|:---|
| **8 个心理陪伴智能体** | 心声树洞、情感解语、深夜心灯、职场加油站、成长伙伴、暖心倾听者、心灵导师、情绪守护者 |
| **5 个专业知识库** | 心理学基础-情绪管理、情绪管理-情绪识别技巧、情感关系-沟通技巧、睡眠与焦虑-失眠应对、职场心理-压力管理 |
| **2 个示例工作流** | 简单安全陪伴流程、情绪识别陪伴流程 |
| **敏感词库** | 涵盖一般敏感词、危机干预词、禁止词等分类 |
| **预配置 API Key** | DeepSeek、智谱 AI、通义千问（已配置，可直接使用） |

---

## 📋 功能特性详解

### 用户端功能

#### 🏠 首页
- 平台介绍与服务理念展示
- 核心功能特色卡片展示
- 快速入口：智能体广场、历史记录

#### 🎭 智能体广场（`/agents`）
- **卡片式展示**：展示已上架智能体，包含头像、名称、简介、标签、热度值
- **关键词搜索**：支持按名称、描述、标签搜索（300ms 防抖优化）
- **排序方式**：热度优先（默认）、最新发布
- **分页浏览**：支持 12/24/48 条每页切换

#### 💬 智能对话（`/agents/:id`）
- **三栏布局**：
  - 左栏：会话列表，支持新建/置顶/重命名/删除会话
  - 中栏：消息区，Markdown 渲染，知识库引用标识
  - 右栏：智能体信息展示
- **智能特性**：
  - 自动携带最近 10 条消息作为上下文
  - 首次对话后自动生成会话标题
  - RAG 知识库检索增强回复质量
  - 工作流执行（安全检测、危机干预）
- **会话管理**：新建、重命名、置顶、删除、清空消息

#### 📜 历史记录（`/history`）
- 按时间倒序展示所有会话
- 支持关键词检索
- 批量删除管理
- 点击查看完整对话详情

#### 👤 个人中心（`/profile`）
- 个人信息编辑（昵称、邮箱、手机）
- 头像上传（支持裁剪）
- 密码修改（需验证原密码）

---

### 管理端功能

#### 🤖 智能体管理（`/admin/agents`）

**列表页**：
- 表格展示所有智能体（头像、名称、状态、热度、创建时间）
- 支持搜索、状态筛选
- 快捷操作：上架/下架、编辑、删除

**详情编辑页（三栏布局）**：
- **左栏 - 基本信息**：
  - 名称、头像上传
  - 简介（富文本）
  - 标签管理（多选）
  - 系统提示词（System Prompt）
  - 问候语配置
- **中栏 - 资源配置**：
  - LLM 模型选择（下拉选择已配置的模型）
  - 知识库绑定（多选，支持拖拽排序）
  - 工作流关联（可选）
  - 参数覆盖（temperature、max_tokens、top_p）
- **右栏 - 测试与发布**：
  - 实时测试对话
  - 状态管理（草稿/已发布）
  - 上架/下架操作

#### 📚 知识库管理（`/admin/knowledge-bases`）

**列表页**：
- 展示知识库名称、描述、文档数、状态
- 状态标识：空/处理中/就绪/部分失败

**详情页**：
- **文档管理**：
  - 上传文档（支持 TXT/MD 格式，最大 50MB）
  - 异步处理状态追踪（pending → processing → completed/failed）
  - 失败重试机制（最多 3 次）
  - 查看切片详情
- **检索测试**：
  - 输入查询语句
  - 返回 Top-K 相似切片
  - 展示相似度分数

**处理流程**：
```
文档上传 → 文本提取 → 智能切片(~500字/片) → 向量化(embedding-2) → ChromaDB存储
```

#### 🔄 工作流管理（`/admin/workflows`）

**可视化编辑器（基于 Vue Flow）**：
- **左侧工具箱**：节点分类展示，支持搜索
- **中间画布**：
  - 拖拽添加节点
  - 连线配置流程
  - 网格对齐
  - 缩放（20%-200%）
  - 撤销/重做（Ctrl+Z/Y）
- **右侧配置面板**：选中节点的参数配置
- **工具栏**：自动布局、验证、保存、发布

**支持的节点类型**：

| 节点 | 类型 | 功能说明 |
|:---|:---|:---|
| 开始 | `start` | 工作流入口，接收用户输入 |
| 文本处理 | `text_process` | 文本清洗与预处理 |
| 安全检测 | `safety_check` | 敏感词检测，输出分支：safe / crisis |
| 危机干预 | `crisis_intervention` | 检测到危机词时推送心理援助热线 |
| 知识检索 | `rag_retrieval` | 向量相似度检索知识库 |
| LLM 处理 | `llm_process` | 调用大模型生成回复 |
| 结束 | `end` | 工作流出口，返回最终结果 |

#### 🧠 模型管理（`/admin/llm`）

**提供商管理**：
- 添加/编辑提供商（DeepSeek、智谱 AI、通义千问、豆包等）
- API Key 配置（AES 加密存储）
- Base URL 配置
- 连接测试

**模型管理**：
- 添加/编辑模型
- 模型类型：Chat / Embedding
- 默认参数配置（temperature、max_tokens、top_p）
- 测试对话功能

**已支持的模型**：

| 提供商 | 模型 | 类型 | API Key 状态 |
|:---|:---|:---|:---:|
| DeepSeek | deepseek-chat | Chat | ✅ 已内置 |
| 智谱 AI | glm-4-flash, glm-4 | Chat | ✅ 已内置 |
| 智谱 AI | embedding-2 | Embedding | ✅ 已内置 |
| 通义千问 | qwen-turbo, qwen-plus | Chat | ✅ 已内置 |
| 豆包 | doubao-pro-32k | Chat | ❌ 需自行配置 |

> 💡 **说明**：DeepSeek、智谱 AI、通义千问的 API Key 已预置在数据库中，部署后可直接使用。豆包需要在管理后台「模型管理」中自行配置 API Key。

#### 🚫 敏感词管理（`/admin/sensitive-words`）

**分类管理**：

| 分类 | 说明 | 推荐动作 |
|:---|:---|:---|
| `general` | 一般敏感词 | replace（替换）或 warn（警告） |
| `crisis` | 危机干预词（如自杀、自残相关） | intervention（触发干预） |
| `prohibited` | 绝对禁止词 | block（直接拦截） |

**动作类型**：

| 动作 | 说明 |
|:---|:---|
| `block` | 直接拦截，返回提示信息 |
| `warn` | 记录警告日志，继续处理 |
| `replace` | 用配置的替换文本替代敏感词 |
| `intervention` | 触发危机干预，推送心理援助热线（400-161-9995） |

**批量操作**：支持批量导入、导出、删除

#### 👥 用户管理（`/admin/users`，仅超级管理员可见）
- 用户列表（头像、用户名、角色、状态、注册时间）
- 创建用户
- 编辑用户信息
- 角色分配（User / Admin / SuperAdmin）
- 启用/禁用账号

---

## 🏗️ 系统架构

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
|:---|:---|:---|
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

---

## 🔧 核心技术实现

### 用户认证与权限

- **JWT Token**：24 小时有效期，HS256 签名算法
- **密码安全**：BCrypt 加密存储
- **接口鉴权**：`@RequireRole` 自定义注解，支持角色级别控制
- **角色体系**：User（普通用户）→ Admin（管理员）→ SuperAdmin（超级管理员）

### RAG 知识检索

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  文档上传    │───►│  文本切片    │───►│   向量化    │───►│ ChromaDB   │
│  TXT/MD     │    │  ~500字/片  │    │ embedding-2 │    │   存储      │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘

┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  用户查询    │───►│   向量化    │───►│ 相似度检索  │───►│ Top-K结果  │
└─────────────┘    └─────────────┘    └─────────────┘    └──────┬──────┘
                                                                │
                                                                ▼
                                                        ┌─────────────┐
                                                        │ 注入提示词  │
                                                        │ LLM 生成    │
                                                        └─────────────┘
```

**技术特点**：
- 异步处理：文档上传后异步向量化，不阻塞用户操作
- 重试机制：失败最多重试 3 次
- 状态追踪：pending → processing → completed/failed

### 工作流引擎

- **执行引擎**：拓扑排序确定节点执行顺序
- **条件分支**：安全检测节点根据结果分流（safe/crisis）
- **数据流转**：节点间通过上下文传递数据
- **危机干预**：检测到危机词自动推送心理援助热线

### 安全风控

- **敏感词检测**：实时匹配检测，支持多种处理动作
- **危机干预**：自动识别并推送专业援助信息
- **API Key 保护**：AES 加密存储，避免明文泄露

### LLM 统一调用

- **接口标准化**：OpenAI 兼容格式
- **多厂商适配**：DeepSeek、智谱、通义、豆包
- **参数可配**：支持 temperature、max_tokens、top_p 等
- **智能体覆盖**：支持智能体级别参数覆盖全局配置

---

## 🗄️ 数据库设计

### ER 关系图

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
      │                    │ ai_agent_knowledge (多对多)
      │                    ▼
      │             ┌─────────────┐
      │             │ai_knowledge │
      │             │    _base    │
      │             ├─────────────┤
      │             │ id          │
      │             │ name        │
      │             │ ...         │
      │             └──────┬──────┘
      │                    │ 一对多
      │                    ▼
      │             ┌─────────────┐
      │             │ai_knowledge │
      │             │  _document  │
      │             └──────┬──────┘
      │                    │ 一对多
      │                    ▼
      │             ┌─────────────┐
      │             │ai_knowledge │
      │             │  _segment   │
      │             └─────────────┘
      │
      ▼
┌─────────────┐     ┌─────────────┐
│chat_session │────►│chat_message │
│   (会话)    │ 1:N │   (消息)    │
└─────────────┘     └─────────────┘
```

### 核心数据表

| 表名 | 说明 | 主要字段 |
|:---|:---|:---|
| `sys_user` | 用户表 | id, username, password, role, status |
| `sys_sensitive_word` | 敏感词表 | id, word, category, action, replacement |
| `llm_provider` | LLM 提供商表 | id, name, api_key, base_url |
| `llm_model` | LLM 模型表 | id, provider_id, name, type, parameters |
| `ai_knowledge_base` | 知识库表 | id, name, description, status |
| `ai_knowledge_document` | 知识文档表 | id, kb_id, filename, status |
| `ai_knowledge_segment` | 知识切片表 | id, doc_id, content, vector_id |
| `ai_workflow` | 工作流表 | id, name, nodes_config, edges_config |
| `ai_agent` | 智能体表 | id, name, system_prompt, model_id, workflow_id |
| `ai_agent_knowledge` | 智能体-知识库关联表 | agent_id, kb_id |
| `chat_session` | 会话表 | id, user_id, agent_id, title |
| `chat_message` | 消息表 | id, session_id, role, content |

---

## 📁 项目结构

```
soulmate/
├── backend/                    # 后端服务 (Spring Boot)
│   ├── src/main/java/com/soulmate/
│   │   ├── controller/         # 控制器层（REST API）
│   │   ├── service/            # 业务逻辑层
│   │   ├── mapper/             # MyBatis 数据访问层
│   │   ├── entity/             # 实体类
│   │   ├── dto/                # 数据传输对象
│   │   ├── config/             # 配置类
│   │   ├── security/           # JWT 认证相关
│   │   ├── annotation/         # 自定义注解（@RequireRole）
│   │   └── common/             # 公共模块（响应封装、异常处理）
│   ├── src/main/resources/
│   │   ├── application.yml     # 主配置
│   │   ├── application-dev.yml # 开发环境配置
│   │   └── application-prod.yml# 生产环境配置
│   ├── Dockerfile.dev
│   └── Dockerfile.prod
│
├── frontend/                   # 前端应用 (Vue 3)
│   ├── src/
│   │   ├── api/                # API 请求封装
│   │   ├── components/         # 公共组件
│   │   ├── views/              # 页面视图
│   │   │   ├── admin/          # 管理端页面
│   │   │   └── *.vue           # 用户端页面
│   │   ├── router/             # 路由配置
│   │   ├── stores/             # Pinia 状态管理
│   │   ├── styles/             # 全局样式
│   │   └── utils/              # 工具函数
│   ├── Dockerfile.dev
│   └── Dockerfile.prod
│
├── rag_service/                # RAG 服务 (FastAPI)
│   ├── app/
│   │   ├── api/                # API 路由
│   │   ├── services/           # 向量化、检索服务
│   │   ├── config.py           # 配置
│   │   └── main.py             # 应用入口
│   ├── data/chroma/            # ChromaDB 持久化数据
│   ├── requirements.txt
│   ├── Dockerfile.dev
│   └── Dockerfile.prod
│
├── deploy_soulmate/            # 部署配置
│   ├── database/
│   │   ├── init/               # 数据库初始化脚本
│   │   │   ├── 00-init.sql     # 字符集配置
│   │   │   ├── 01-schema.sql   # 表结构
│   │   │   └── 02-data.sql     # 初始数据
│   │   └── 示例文档/            # 知识库示例文档
│   ├── docker-compose-dev.yml  # 开发环境编排
│   ├── docker-compose-prod.yml # 生产环境编排
│   ├── env.dev.example         # 开发环境变量模板
│   ├── env.prod.example        # 生产环境变量模板
│   └── Makefile                # 快捷命令
│
├── docs/                       # 项目文档
│   ├── 01用户需求分析.md
│   ├── 02数据库设计.md
│   ├── 03API接口设计.md
│   └── 04开发指南.md
│
└── README.md
```

### 代码规模

| 模块 | 文件数 | 主要功能 |
|:---|:---|:---|
| **后端** | ~80 | 用户认证、智能体、知识库、工作流、对话、LLM 管理 |
| **前端** | ~30 | 用户端 7 页面 + 管理端 13 页面 + 工作流编辑器 |
| **RAG 服务** | ~6 | 向量化、检索、文档处理 |

---

## 🌐 API 概览

### 接口分类

| 模块 | 基础路径 | 说明 |
|:---|:---|:---|
| 认证 | `/api/v1/auth` | 登录、注册、登出 |
| 个人中心 | `/api/v1/profile` | 个人信息管理 |
| 智能体 | `/api/v1/agents` | 智能体 CRUD |
| 知识库 | `/api/v1/knowledge-bases` | 知识库管理 |
| 工作流 | `/api/v1/workflows` | 工作流管理 |
| LLM 模型 | `/api/v1/llm` | 提供商/模型管理 |
| 敏感词 | `/api/v1/sensitive-words` | 敏感词管理 |
| 会话 | `/api/v1/sessions` | 会话管理 |
| 消息 | `/api/v1/sessions/{id}/messages` | 消息收发 |
| 用户管理 | `/api/v1/users` | 用户 CRUD（仅 SuperAdmin） |
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

### 权限矩阵

| 接口 | 公开 | 登录用户 | Admin | SuperAdmin |
|:---|:---:|:---:|:---:|:---:|
| 认证接口 | ✅ | - | - | - |
| 智能体列表（已上架） | ✅ | - | - | - |
| 智能体管理 | - | - | ✅ | ✅ |
| 知识库/工作流/模型/敏感词 | - | - | ✅ | ✅ |
| 会话/消息 | - | ✅ | - | - |
| 用户管理 | - | - | - | ✅ |

---

## ❓ 常见问题

### Q1: 服务启动失败

**检查步骤**：

```bash
# 1. 检查容器状态
docker compose -f docker-compose-dev.yml --env-file .env.dev ps

# 2. 检查端口占用（Windows）
netstat -an | findstr "3000 8081 8000 3307"

# 3. 查看详细日志
docker compose -f docker-compose-dev.yml --env-file .env.dev logs -f

# 4. 单独查看某个服务日志
docker logs soulmate-dev-backend
docker logs soulmate-dev-rag-service
docker logs soulmate-dev-mysql
```

**常见原因与解决**：
- **端口被占用**：修改 `.env.dev` 中的端口配置，或关闭占用端口的程序
- **内存不足**：Docker 至少需要 4GB 内存，请在 Docker Desktop 设置中调整
- **首次构建慢**：Maven/npm 下载依赖需要时间，请耐心等待
- **网络问题**：如遇依赖下载失败，可配置国内镜像源

### Q2: 对话无响应

1. 系统已预置 API Key，通常无需额外配置
2. 查看后端日志：`docker logs soulmate-dev-backend`
3. 确认智能体已关联有效的模型
4. 检查网络是否能访问 LLM API（如 api.deepseek.com）

### Q3: 知识库检索无结果

1. 确认文档处理状态为 `completed`（在知识库详情页查看）
2. 确认知识库已绑定到智能体
3. 检查 RAG 服务日志：`docker logs soulmate-dev-rag-service`
4. 使用知识库管理中的「检索测试」功能验证

### Q4: 工作流执行失败

1. 检查工作流是否已发布（status = published）
2. 验证节点配置是否完整（必填项不能为空）
3. 确保工作流中的模型配置有效

### Q5: 数据库连接失败

```bash
# 检查 MySQL 容器状态
docker logs soulmate-dev-mysql

# 手动连接测试
docker exec -it soulmate-dev-mysql mysql -uroot -pdev123456
```

---

## 📚 项目文档

| 文档 | 说明 |
|:---|:---|
| [用户需求分析](docs/01用户需求分析.md) | 完整功能需求规格说明 |
| [数据库设计](docs/02数据库设计.md) | 数据库表结构详细设计 |
| [API 接口设计](docs/03API接口设计.md) | RESTful API 接口文档 |
| [开发指南](docs/04开发指南.md) | 开发阶段划分与任务清单 |

---

## 📄 更新日志

### v1.0.0 (2026-01-06)

**首个正式版本发布**

- ✅ 用户认证与权限管理（JWT + BCrypt + 角色体系）
- ✅ 智能体全生命周期管理（创建、编辑、资源配置、上下架）
- ✅ RAG 知识库系统（文档上传、异步切片、向量检索）
- ✅ 可视化工作流编辑器（Vue Flow、节点拖拽、条件分支）
- ✅ 多厂商 LLM 支持（DeepSeek、智谱、通义、豆包）
- ✅ 敏感词风控机制（检测、干预、替换、拦截）
- ✅ 智能对话系统（上下文管理、知识增强、工作流执行）
- ✅ Docker Compose 一键部署
- ✅ 预置 8 个智能体 + 5 个知识库 + 2 个工作流

---

<p align="center">
  <strong>🌸 SoulMate 心伴 - 用心陪伴每一刻</strong>
</p>

<p align="center">
  <sub>文档版本：v1.0 | 最后更新：2026-01-07</sub>
</p>
