# 🔌 SoulMate 心伴 - API 接口设计文档

> **SoulMate** - 您的AI心灵伴侣

## 📊 统一响应格式

### 成功响应格式

```json
{
  "code": 0,
  "message": "操作成功",
  "data": {},
  "timestamp": 1634567890123
}
```

### 错误响应格式

```json
{
  "code": 400,
  "message": "请求参数错误",
  "data": null,
  "timestamp": 1634567890123
}
```

### 响应状态码说明

| HTTP状态码 | 业务状态码 | 说明 | 描述 |
|-----------|-----------|------|------|
| 200 | 0 | SUCCESS | 操作成功 |
| 400 | 1001 | PARAM_ERROR | 请求参数错误 |
| 401 | 2001 | UNAUTHORIZED | 未认证或认证失败 |
| 403 | 2003 | ACCESS_DENIED | 权限不足 |
| 404 | - | NOT_FOUND | 资源不存在 |
| 500 | 1002 | SYSTEM_ERROR | 服务器内部错误 |

### 分页响应格式

```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "list": [],
    "total": 100,
    "page": 1,
    "size": 20,
    "pages": 5
  },
  "timestamp": 1634567890123
}
```

### 请求头

```
Authorization: Bearer {token}    // 需要鉴权的接口
Content-Type: application/json   // POST/PUT 请求
```

### 分页参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| `page` | int | ❌ | 1 | 页码 |
| `size` | int | ❌ | 20 | 每页数量 |

---

# 📚 API接口详细文档

## 🔑 认证相关接口

### 用户注册

**接口地址**: `POST /api/v1/auth/register`

**控制器**: `AuthController.register()`

**请求参数**:
```json
{
  "username": "user123",
  "password": "password123",
  "nickname": "昵称"
}
```

**参数验证**:
- `username`: 必填，3-50字符
- `password`: 必填，6-20字符
- `nickname`: 可选

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "userId": "usr_xxx",
    "username": "user123",
    "role": "user",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400
  },
  "timestamp": 1634567890123
}
```

**错误响应**:
```json
{
  "code": 3002,
  "message": "用户名已存在",
  "data": null,
  "timestamp": 1634567890123
}
```

### 用户登录

**接口地址**: `POST /api/v1/auth/login`

**控制器**: `AuthController.login()`

**请求参数**:
```json
{
  "username": "user123",
  "password": "password123"
}
```

**参数验证**:
- `username`: 必填
- `password`: 必填

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "userId": "usr_xxx",
    "username": "user123",
    "role": "user",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400
  },
  "timestamp": 1634567890123
}
```

**错误响应**:
```json
{
  "code": 3003,
  "message": "密码错误",
  "data": null,
  "timestamp": 1634567890123
}
```

### 获取当前用户信息

**接口地址**: `GET /api/v1/auth/me`

**控制器**: `AuthController.me()`

**请求头**: `Authorization: Bearer {token}`

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "id": "usr_xxx",
    "username": "user123",
    "role": "user",
    "nickname": "昵称",
    "avatar": "https://...",
    "bio": "个人简介",
    "status": 1
  },
  "timestamp": 1634567890123
}
```

### 用户登出

**接口地址**: `POST /api/v1/auth/logout`

**控制器**: `AuthController.logout()`

**请求头**: `Authorization: Bearer {token}`

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

---

## 👥 用户管理接口 (SuperAdmin)

**权限说明**: 只有 SuperAdmin 可以访问用户管理接口，Admin 和 User 均无权限。

### 获取用户列表

**接口地址**: `GET /api/v1/users`

**控制器**: `UserController.listUsers()`

**权限要求**: SuperAdmin

**请求参数**:
- `page`: 页码，默认1
- `size`: 每页大小，默认20
- `role`: 角色筛选（可选）
- `status`: 状态筛选（可选）
- `keyword`: 搜索关键词（可选）

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "list": [
      {
        "id": "usr_xxx",
        "username": "user123",
        "role": "user",
        "nickname": "昵称",
        "avatar": "https://...",
        "status": 1,
        "createdAt": "2024-01-01T00:00:00"
      }
    ],
    "total": 100,
    "page": 1,
    "size": 20,
    "pages": 5
  },
  "timestamp": 1634567890123
}
```

### 获取用户详情

**接口地址**: `GET /api/v1/users/{id}`

**控制器**: `UserController.getUserById()`

**权限要求**: SuperAdmin

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "id": "usr_xxx",
    "username": "user123",
    "role": "user",
    "nickname": "昵称",
    "avatar": "https://...",
    "bio": "个人简介",
    "status": 1,
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  },
  "timestamp": 1634567890123
}
```

### 更新用户

**接口地址**: `PUT /api/v1/users/{id}`

**控制器**: `UserController.updateUser()`

**权限要求**: SuperAdmin

**请求参数**:
```json
{
  "nickname": "新昵称",
  "avatar": "https://...",
  "bio": "新简介",
  "role": "admin",
  "status": 1
}
```

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

### 删除用户

**接口地址**: `DELETE /api/v1/users/{id}`

**控制器**: `UserController.deleteUser()`

**权限要求**: SuperAdmin

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

### 启用/禁用用户

**接口地址**: `PUT /api/v1/users/{id}/status`

**控制器**: `UserController.updateUserStatus()`

**权限要求**: SuperAdmin

**请求参数**:
```json
{
  "status": 0
}
```

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

---

## 👤 个人中心接口

### 获取个人信息

**接口地址**: `GET /api/v1/profile`

**控制器**: `ProfileController.getProfile()`

**请求头**: `Authorization: Bearer {token}`

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "id": "usr_xxx",
    "username": "user123",
    "role": "user",
    "nickname": "昵称",
    "avatar": "https://...",
    "bio": "个人简介",
    "status": 1
  },
  "timestamp": 1634567890123
}
```

### 更新个人信息

**接口地址**: `PUT /api/v1/profile`

**控制器**: `ProfileController.updateProfile()`

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "nickname": "新昵称",
  "bio": "新简介"
}
```

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

### 修改密码

**接口地址**: `PUT /api/v1/profile/password`

**控制器**: `ProfileController.changePassword()`

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "oldPassword": "旧密码",
  "newPassword": "新密码"
}
```

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

---

## 🤖 智能体管理接口

**权限说明**:
- 列表和详情接口：公开访问（无需登录）
- 创建/更新/删除/上架接口：需要 Admin 或 SuperAdmin 权限

### 获取智能体列表（广场）

**接口地址**: `GET /api/v1/agents`

**控制器**: `AgentController.listAgents()`

**请求参数**:
- `page`: 页码，默认1
- `size`: 每页大小，默认20
- `status`: 状态筛选（可选，如：published）
- `tag`: 标签筛选（可选）
- `keyword`: 关键词搜索（可选）
- `sort`: 排序（可选，如：heat, createTime, name）

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "list": [
      {
        "id": "agt_xxx",
        "name": "暖心陪伴",
        "avatar": "https://...",
        "description": "温暖的心理陪伴伙伴",
        "tags": ["情感", "职场"],
        "heatValue": 1234,
        "status": "published",
        "createdAt": "2024-01-01T00:00:00"
      }
    ],
    "total": 50,
    "page": 1,
    "size": 20,
    "pages": 3
  },
  "timestamp": 1634567890123
}
```

### 获取智能体详情

**接口地址**: `GET /api/v1/agents/{id}`

**控制器**: `AgentController.getAgent()`

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "id": "agt_xxx",
    "name": "暖心陪伴",
    "avatar": "https://...",
    "description": "温暖的心理陪伴伙伴",
    "tags": ["情感", "职场"],
    "greeting": "你好，我是你的心理陪伴伙伴...",
    "systemPrompt": "你是一位温暖、专业的心理陪伴师...",
    "modelId": "mdl_deepseek_chat",
    "modelConfig": {
      "temperature": 0.8,
      "maxTokens": 1500
    },
    "workflowId": "wfl_xxx",
    "status": "published",
    "heatValue": 1234,
    "createdAt": "2024-01-01T00:00:00",
    "publishedAt": "2024-01-02T00:00:00"
  },
  "timestamp": 1634567890123
}
```

### 创建智能体

**接口地址**: `POST /api/v1/agents`

**控制器**: `AgentController.createAgent()`

**权限要求**: Admin 或 SuperAdmin

**请求参数**:
```json
{
  "name": "暖心陪伴",
  "avatar": "https://...",
  "description": "温暖的心理陪伴伙伴",
  "tags": "情感,职场",
  "greeting": "你好，我是...",
  "systemPrompt": "你是一位...",
  "modelId": "mdl_deepseek_chat",
  "modelConfig": {
    "temperature": 0.8,
    "maxTokens": 1500
  },
  "workflowId": "wfl_xxx",
  "kbIds": ["kb_xxx"]
}
```

**参数验证**:
- `name`: 必填
- `greeting`: 必填
- `systemPrompt`: 必填
- `modelId`: 必填

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "id": "agt_xxx"
  },
  "timestamp": 1634567890123
}
```

### 更新智能体

**接口地址**: `PUT /api/v1/agents/{id}`

**控制器**: `AgentController.updateAgent()`

**权限要求**: Admin 或 SuperAdmin

**请求参数**: 同创建接口（所有字段可选）

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

### 删除智能体

**接口地址**: `DELETE /api/v1/agents/{id}`

**控制器**: `AgentController.deleteAgent()`

**权限要求**: Admin 或 SuperAdmin

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

### 上架/下架智能体

**接口地址**: `PUT /api/v1/agents/{id}/status`

**控制器**: `AgentController.updateAgentStatus()`

**权限要求**: Admin 或 SuperAdmin

**请求参数**:
```json
{
  "status": "published"
}
```

**说明**:
- 智能体只有两种状态：`published`（已上架）和 `offline`（已下架）
- 新建智能体默认状态为 `offline`（下架）
- 只有 `published` 状态的智能体才能在广场展示
- 上架时记录 `published_at` 时间戳

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

### 绑定知识库

**接口地址**: `POST /api/v1/agents/{id}/knowledge`

**控制器**: `AgentController.bindKnowledgeBases()`

**权限要求**: Admin 或 SuperAdmin

**请求参数**:
```json
{
  "kbIds": ["kb_xxx", "kb_yyy"]
}
```

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

### 解绑知识库

**接口地址**: `DELETE /api/v1/agents/{id}/knowledge/{kbId}`

**控制器**: `AgentController.unbindKnowledgeBase()`

**权限要求**: Admin 或 SuperAdmin

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

### 获取智能体绑定的知识库列表

**接口地址**: `GET /api/v1/agents/{id}/knowledge`

**控制器**: `AgentController.getAgentKnowledgeBases()`

**权限要求**: Admin 或 SuperAdmin

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": [
    {
      "id": "kb_xxx",
      "name": "心理学知识库",
      "description": "..."
    }
  ],
  "timestamp": 1634567890123
}
```

---

## 📚 知识库管理接口 (Admin)

### 获取知识库列表

**接口地址**: `GET /api/v1/knowledge-bases`

**控制器**: `KnowledgeBaseController.listKnowledgeBases()`

**权限要求**: Admin 或 SuperAdmin

**请求参数**:
- `page`: 页码，默认1
- `size`: 每页大小，默认20
- `keyword`: 搜索关键词（可选）

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "list": [
      {
        "id": "kb_xxx",
        "name": "心理学知识库",
        "description": "心理学相关文档",
        "docCount": 10,
        "segmentCount": 500,
        "isActive": 1,
        "createdAt": "2024-01-01T00:00:00"
      }
    ],
    "total": 20,
    "page": 1,
    "size": 20,
    "pages": 1
  },
  "timestamp": 1634567890123
}
```

### 获取知识库详情

**接口地址**: `GET /api/v1/knowledge-bases/{id}`

**控制器**: `KnowledgeBaseController.getKnowledgeBase()`

**权限要求**: Admin 或 SuperAdmin

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "id": "kb_xxx",
    "name": "心理学知识库",
    "description": "心理学相关文档",
    "embeddingModel": "mdl_embedding",
    "docCount": 10,
    "segmentCount": 500,
    "isActive": 1,
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  },
  "timestamp": 1634567890123
}
```

### 创建知识库

**接口地址**: `POST /api/v1/knowledge-bases`

**控制器**: `KnowledgeBaseController.createKnowledgeBase()`

**权限要求**: Admin 或 SuperAdmin

**请求参数**:
```json
{
  "name": "心理学知识库",
  "description": "心理学相关文档",
  "embeddingModel": "mdl_embedding"
}
```

**参数验证**:
- `name`: 必填

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "id": "kb_xxx"
  },
  "timestamp": 1634567890123
}
```

### 更新知识库

**接口地址**: `PUT /api/v1/knowledge-bases/{id}`

**控制器**: `KnowledgeBaseController.updateKnowledgeBase()`

**权限要求**: Admin 或 SuperAdmin

**请求参数**:
```json
{
  "name": "新名称",
  "description": "新描述"
}
```

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

### 删除知识库

**接口地址**: `DELETE /api/v1/knowledge-bases/{id}`

**控制器**: `KnowledgeBaseController.deleteKnowledgeBase()`

**权限要求**: Admin 或 SuperAdmin

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

### 上传文档

**接口地址**: `POST /api/v1/knowledge-bases/{id}/documents`

**控制器**: `KnowledgeBaseController.uploadDocument()`

**权限要求**: Admin 或 SuperAdmin

**请求**: `multipart/form-data`
- `file`: 文档文件（必填，TXT/MD/PDF/DOC/DOCX，最大50MB）

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "docId": 123
  },
  "timestamp": 1634567890123
}
```

### 获取文档列表

**接口地址**: `GET /api/v1/knowledge-bases/{id}/documents`

**控制器**: `KnowledgeBaseController.listDocuments()`

**权限要求**: Admin 或 SuperAdmin

**请求参数**:
- `page`: 页码，默认1
- `size`: 每页大小，默认20
- `status`: 状态筛选（可选）

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "list": [
      {
        "id": 123,
        "fileName": "document.pdf",
        "fileType": "pdf",
        "fileSize": 1024000,
        "segmentCount": 50,
        "status": "completed",
        "createdAt": "2024-01-01T00:00:00"
      }
    ],
    "total": 10,
    "page": 1,
    "size": 20,
    "pages": 1
  },
  "timestamp": 1634567890123
}
```

### 删除文档

**接口地址**: `DELETE /api/v1/knowledge-bases/{id}/documents/{docId}`

**控制器**: `KnowledgeBaseController.deleteDocument()`

**权限要求**: Admin 或 SuperAdmin

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

### 查询文档处理状态

**接口地址**: `GET /api/v1/knowledge-bases/{id}/documents/{docId}/status`

**控制器**: `KnowledgeBaseController.getDocumentStatus()`

**权限要求**: Admin 或 SuperAdmin

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "id": 123,
    "fileName": "document.pdf",
    "status": "processing",
    "segmentCount": 0,
    "retryCount": 0,
    "errorMessage": null,
    "createdAt": "2024-01-01T00:00:00"
  },
  "timestamp": 1634567890123
}
```

**说明**:
- 文档上传后异步处理，可通过此接口查询处理状态
- 建议前端轮询此接口（如每3秒查询一次），直到状态为 `completed` 或 `failed`
- 状态值：`pending`（待处理）、`processing`（处理中）、`completed`（已完成）、`failed`（失败）

### 重试文档处理

**接口地址**: `POST /api/v1/knowledge-bases/{id}/documents/{docId}/retry`

**控制器**: `KnowledgeBaseController.retryDocument()`

**权限要求**: Admin 或 SuperAdmin

**说明**:
- 仅当文档状态为 `failed` 时可重试
- 重试后状态变为 `processing`，然后重新执行切片和向量化流程
- 最多重试3次，超过3次需人工处理

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

### 测试检索功能

**接口地址**: `POST /api/v1/knowledge-bases/{id}/test`

**控制器**: `KnowledgeBaseController.testRetrieval()`

**权限要求**: Admin 或 SuperAdmin

**请求参数**:
```json
{
  "query": "如何缓解焦虑",
  "topK": 3
}
```

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": [
    {
      "text": "缓解焦虑的方法包括...",
      "score": 0.85,
      "metadata": {
        "kbId": "kb_xxx",
        "docId": 123,
        "segmentId": 456
      }
    }
  ],
  "timestamp": 1634567890123
}
```

### 更新知识库状态

**接口地址**: `PUT /api/v1/knowledge-bases/{id}/status`

**控制器**: `KnowledgeBaseController.updateKnowledgeBaseStatus()`

**权限要求**: Admin 或 SuperAdmin

**请求参数**:
```json
{
  "isActive": 1,
  "disableRelatedAgents": false
}
```

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

### 统计关联的智能体数量

**接口地址**: `GET /api/v1/knowledge-bases/{id}/related-agents-count`

**控制器**: `KnowledgeBaseController.countRelatedAgents()`

**权限要求**: Admin 或 SuperAdmin

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "count": 5
  },
  "timestamp": 1634567890123
}
```

---

## 🔄 工作流管理接口 (Admin)

### 获取工作流列表

**接口地址**: `GET /api/v1/workflows`

**控制器**: `WorkflowController.listWorkflows()`

**权限要求**: Admin 或 SuperAdmin

**请求参数**:
- `page`: 页码，默认1
- `size`: 每页大小，默认20
- `keyword`: 搜索关键词（可选）
- `isActive`: 是否启用（可选）

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "list": [
      {
        "id": "wfl_xxx",
        "name": "心理陪伴工作流",
        "description": "...",
        "status": "published",
        "isActive": 1,
        "nodeCount": 5,
        "hasRag": 1,
        "hasCrisisIntervention": 1,
        "createdAt": "2024-01-01T00:00:00"
      }
    ],
    "total": 20,
    "page": 1,
    "size": 20,
    "pages": 1
  },
  "timestamp": 1634567890123
}
```

### 获取工作流详情

**接口地址**: `GET /api/v1/workflows/{id}`

**控制器**: `WorkflowController.getWorkflow()`

**权限要求**: Admin 或 SuperAdmin

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "id": "wfl_xxx",
    "name": "心理陪伴工作流",
    "description": "...",
    "nodesConfig": {
      "nodes": [...],
      "edges": [...]
    },
    "status": "published",
    "validationStatus": "valid",
    "isActive": 1,
    "nodeCount": 5,
    "hasRag": 1,
    "hasCrisisIntervention": 1,
    "createdBy": "usr_admin",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  },
  "timestamp": 1634567890123
}
```

### 创建工作流

**接口地址**: `POST /api/v1/workflows`

**控制器**: `WorkflowController.createWorkflow()`

**权限要求**: Admin 或 SuperAdmin

**请求参数**:
```json
{
  "name": "心理陪伴工作流",
  "description": "...",
  "nodesConfig": {
    "nodes": [
      {
        "id": "start",
        "type": "start",
        "name": "开始",
        "config": {},
        "position": { "x": 100, "y": 200 }
      },
      {
        "id": "llm",
        "type": "llm_process",
        "name": "LLM处理",
        "config": {
          "modelId": "mdl_xxx"
        },
        "position": { "x": 300, "y": 200 }
      }
    ],
    "edges": [
      { "source": "start", "target": "llm" }
    ]
  },
  "isActive": 1
}
```

**参数验证**:
- `name`: 必填
- `nodesConfig`: 必填

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "id": "wfl_xxx"
  },
  "timestamp": 1634567890123
}
```

### 更新工作流

**接口地址**: `PUT /api/v1/workflows/{id}`

**控制器**: `WorkflowController.updateWorkflow()`

**权限要求**: Admin 或 SuperAdmin

**请求参数**: 同创建接口

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

### 删除工作流

**接口地址**: `DELETE /api/v1/workflows/{id}`

**控制器**: `WorkflowController.deleteWorkflow()`

**权限要求**: Admin 或 SuperAdmin

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

### 启用/禁用工作流

**接口地址**: `PUT /api/v1/workflows/{id}/status`

**控制器**: `WorkflowController.updateWorkflowStatus()`

**权限要求**: Admin 或 SuperAdmin

**请求参数**:
```json
{
  "isActive": 1,
  "disableRelatedAgents": false
}
```

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

### 查询关联的智能体数量

**接口地址**: `GET /api/v1/workflows/{id}/related-agents-count`

**控制器**: `WorkflowController.countRelatedAgents()`

**权限要求**: Admin 或 SuperAdmin

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "count": 3
  },
  "timestamp": 1634567890123
}
```

### 获取节点定义列表

**接口地址**: `GET /api/v1/workflows/node-definitions`

**控制器**: `WorkflowController.getNodeDefinitions()`

**权限要求**: Admin 或 SuperAdmin

**说明**: 返回所有可用的节点类型定义，包含节点名称、图标、颜色、配置模板等信息。

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": [
    {
      "type": "start",
      "name": "开始",
      "description": "工作流入口节点",
      "icon": "play",
      "color": "#52c41a",
      "configSchema": {}
    },
    {
      "type": "llm_process",
      "name": "LLM处理",
      "description": "调用大语言模型生成回复",
      "icon": "robot",
      "color": "#1890ff",
      "configSchema": {
        "modelId": {
          "type": "string",
          "required": true,
          "label": "模型ID"
        }
      }
    }
  ],
  "timestamp": 1634567890123
}
```

### 验证工作流配置

**接口地址**: `POST /api/v1/workflows/validate`

**控制器**: `WorkflowController.validateWorkflow()`

**权限要求**: Admin 或 SuperAdmin

**请求参数**:
```json
{
  "nodesConfig": {
    "nodes": [...],
    "edges": [...]
  }
}
```

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "valid": true,
    "status": "valid",
    "errors": [],
    "warnings": []
  },
  "timestamp": 1634567890123
}
```

---

## 🤖 LLM模型管理接口 (Admin)

### 获取提供商列表

**接口地址**: `GET /api/v1/llm/providers`

**控制器**: `LlmProviderController.listProviders()`

**权限要求**: Admin 或 SuperAdmin

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": [
    {
      "id": "prv_deepseek",
      "name": "DeepSeek",
      "apiBase": "https://api.deepseek.com",
      "isActive": 1
    }
  ],
  "timestamp": 1634567890123
}
```

### 创建提供商

**接口地址**: `POST /api/v1/llm/providers`

**控制器**: `LlmProviderController.createProvider()`

**权限要求**: Admin 或 SuperAdmin

**请求参数**:
```json
{
  "name": "DeepSeek",
  "apiBase": "https://api.deepseek.com",
  "apiKey": "sk-xxx",
  "isActive": 1
}
```

**参数验证**:
- `name`: 必填

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "id": "prv_deepseek"
  },
  "timestamp": 1634567890123
}
```

### 更新提供商

**接口地址**: `PUT /api/v1/llm/providers/{id}`

**控制器**: `LlmProviderController.updateProvider()`

**权限要求**: Admin 或 SuperAdmin

**请求参数**: 同创建接口（所有字段可选）

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

### 删除提供商

**接口地址**: `DELETE /api/v1/llm/providers/{id}`

**控制器**: `LlmProviderController.deleteProvider()`

**权限要求**: Admin 或 SuperAdmin

**说明**: 如果提供商下有关联的模型，需先删除模型才能删除提供商。

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

### 获取模型列表

**接口地址**: `GET /api/v1/llm/models`

**控制器**: `LlmModelController.listModels()`

**权限要求**: Admin 或 SuperAdmin

**请求参数**:
- `providerId`: 提供商ID筛选（可选）
- `modelType`: 模型类型筛选（可选，如：chat, embedding）
- `isActive`: 是否启用（可选）

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": [
    {
      "id": "mdl_deepseek_chat",
      "providerId": "prv_deepseek",
      "name": "deepseek-chat",
      "displayName": "DeepSeek Chat",
      "modelType": "chat",
      "apiBase": "https://api.deepseek.com/v1",
      "defaultConfig": {
        "temperature": 0.7,
        "topP": 0.9,
        "maxTokens": 2000
      },
      "isActive": 1
    }
  ],
  "timestamp": 1634567890123
}
```

### 创建模型

**接口地址**: `POST /api/v1/llm/models`

**控制器**: `LlmModelController.createModel()`

**权限要求**: Admin 或 SuperAdmin

**请求参数**:
```json
{
  "providerId": "prv_deepseek",
  "name": "deepseek-chat",
  "displayName": "DeepSeek Chat",
  "modelType": "chat",
  "apiBase": "https://...",
  "apiKey": "sk-xxx",
  "defaultConfig": {
    "temperature": 0.7,
    "topP": 0.9,
    "maxTokens": 2000
  },
  "isActive": 1
}
```

**参数验证**:
- `providerId`: 必填
- `name`: 必填
- `displayName`: 必填
- `modelType`: 必填（chat | embedding）

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "id": "mdl_deepseek_chat"
  },
  "timestamp": 1634567890123
}
```

### 更新模型

**接口地址**: `PUT /api/v1/llm/models/{id}`

**控制器**: `LlmModelController.updateModel()`

**权限要求**: Admin 或 SuperAdmin

**请求参数**: 同创建接口（所有字段可选）

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

### 删除模型

**接口地址**: `DELETE /api/v1/llm/models/{id}`

**控制器**: `LlmModelController.deleteModel()`

**权限要求**: Admin 或 SuperAdmin

**说明**: 删除前需检查是否被智能体使用。

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

### 测试对话

**接口地址**: `POST /api/v1/llm/models/test-chat`

**控制器**: `LlmModelController.testChat()`

**权限要求**: Admin 或 SuperAdmin

**请求参数**:
```json
{
  "modelId": "mdl_xxx",
  "message": "你好",
  "systemPrompt": "你是一个助手"
}
```

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "response": "你好！有什么可以帮助你的吗？"
  },
  "timestamp": 1634567890123
}
```

### 查询关联的智能体数量

**接口地址**: `GET /api/v1/llm/models/{id}/related-agents-count`

**控制器**: `LlmModelController.countRelatedAgents()`

**权限要求**: Admin 或 SuperAdmin

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "count": 5
  },
  "timestamp": 1634567890123
}
```

---

## 🚫 敏感词管理接口 (Admin)

### 获取敏感词列表

**接口地址**: `GET /api/v1/sensitive-words`

**控制器**: `SensitiveWordController.listSensitiveWords()`

**权限要求**: Admin 或 SuperAdmin

**请求参数**:
- `page`: 页码，默认1
- `size`: 每页大小，默认20
- `category`: 分类筛选（可选，如：general, crisis, prohibited）
- `action`: 处理动作筛选（可选）
- `isActive`: 是否启用（可选）
- `keyword`: 搜索关键词（可选）

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "list": [
      {
        "id": 1,
        "word": "自杀",
        "category": "crisis",
        "action": "warn",
        "replacement": null,
        "isActive": 1,
        "createdAt": "2024-01-01T00:00:00"
      }
    ],
    "total": 100,
    "page": 1,
    "size": 20,
    "pages": 5
  },
  "timestamp": 1634567890123
}
```

### 创建敏感词

**接口地址**: `POST /api/v1/sensitive-words`

**控制器**: `SensitiveWordController.createSensitiveWord()`

**权限要求**: Admin 或 SuperAdmin

**请求参数**:
```json
{
  "word": "自杀",
  "category": "crisis",
  "action": "intervention",
  "replacement": "***",
  "isActive": 1
}
```

**参数验证**:
- `word`: 必填
- `category`: 必填（general | crisis | prohibited）
- `action`: 必填（block | warn | replace | intervention）

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "id": 1
  },
  "timestamp": 1634567890123
}
```

### 更新敏感词

**接口地址**: `PUT /api/v1/sensitive-words/{id}`

**控制器**: `SensitiveWordController.updateSensitiveWord()`

**权限要求**: Admin 或 SuperAdmin

**请求参数**: 同创建接口（所有字段可选）

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

### 删除敏感词

**接口地址**: `DELETE /api/v1/sensitive-words/{id}`

**控制器**: `SensitiveWordController.deleteSensitiveWord()`

**权限要求**: Admin 或 SuperAdmin

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

### 更新敏感词状态

**接口地址**: `PUT /api/v1/sensitive-words/{id}/status`

**控制器**: `SensitiveWordController.updateSensitiveWordStatus()`

**权限要求**: Admin 或 SuperAdmin

**请求参数**:
```json
{
  "isActive": 1
}
```

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

### 批量更新敏感词状态

**接口地址**: `PUT /api/v1/sensitive-words/batch/status`

**控制器**: `SensitiveWordController.batchUpdateStatus()`

**权限要求**: Admin 或 SuperAdmin

**请求参数**:
```json
{
  "ids": [1, 2, 3],
  "isActive": 1
}
```

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

### 批量删除敏感词

**接口地址**: `DELETE /api/v1/sensitive-words/batch`

**控制器**: `SensitiveWordController.batchDelete()`

**权限要求**: Admin 或 SuperAdmin

**请求参数**:
```json
{
  "ids": [1, 2, 3]
}
```

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

---

## 💬 会话管理接口

### 获取会话列表

**接口地址**: `GET /api/v1/sessions`

**控制器**: `SessionController.listSessions()`

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
- `page`: 页码，默认1
- `size`: 每页大小，默认20
- `agentId`: 智能体ID筛选（可选）
- `sessionType`: 会话类型筛选（可选，如：normal, debug）

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "list": [
      {
        "id": 123,
        "agentId": "agt_xxx",
        "agentName": "暖心陪伴",
        "agentAvatar": "https://...",
        "title": "关于焦虑的对话",
        "sessionType": "normal",
        "isPinned": 0,
        "messageCount": 10,
        "updatedAt": "2024-01-01T12:00:00"
      }
    ],
    "total": 50,
    "page": 1,
    "size": 20,
    "pages": 3
  },
  "timestamp": 1634567890123
}
```

### 创建会话

**接口地址**: `POST /api/v1/sessions`

**控制器**: `SessionController.createSession()`

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "agentId": "agt_xxx",
  "title": "新对话",
  "sessionType": "normal"
}
```

**参数验证**:
- `agentId`: 必填

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "id": 123,
    "agentId": "agt_xxx",
    "title": "新对话",
    "greeting": "你好，我是你的心理陪伴伙伴..."
  },
  "timestamp": 1634567890123
}
```

### 获取会话详情

**接口地址**: `GET /api/v1/sessions/{id}`

**控制器**: `SessionController.getSession()`

**请求头**: `Authorization: Bearer {token}`

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "id": 123,
    "userId": "usr_xxx",
    "agentId": "agt_xxx",
    "title": "关于焦虑的对话",
    "sessionType": "normal",
    "isPinned": 0,
    "messageCount": 10,
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T12:00:00"
  },
  "timestamp": 1634567890123
}
```

### 更新会话标题

**接口地址**: `PUT /api/v1/sessions/{id}/title`

**控制器**: `SessionController.updateSessionTitle()`

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "title": "新标题"
}
```

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

### 置顶/取消置顶

**接口地址**: `PUT /api/v1/sessions/{id}/pin`

**控制器**: `SessionController.updateSessionPinned()`

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "isPinned": 1
}
```

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

### 删除会话

**接口地址**: `DELETE /api/v1/sessions/{id}`

**控制器**: `SessionController.deleteSession()`

**请求头**: `Authorization: Bearer {token}`

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null,
  "timestamp": 1634567890123
}
```

---

## 📨 消息管理接口

### 获取消息列表

**接口地址**: `GET /api/v1/sessions/{sessionId}/messages`

**控制器**: `MessageController.listMessages()`

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
- `page`: 页码，默认1
- `size`: 每页大小，默认50（建议50-100）

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "list": [
      {
        "id": 1,
        "role": "assistant",
        "content": "你好，我是你的心理陪伴伙伴...",
        "msgType": "greeting",
        "emotion": null,
        "createdAt": "2024-01-01T00:00:00"
      },
      {
        "id": 2,
        "role": "user",
        "content": "我今天心情很不好",
        "msgType": "text",
        "emotion": null,
        "createdAt": "2024-01-01T00:05:00"
      },
      {
        "id": 3,
        "role": "assistant",
        "content": "我理解你现在的心情...",
        "msgType": "text",
        "emotion": "悲伤",
        "tokenCount": 150,
        "createdAt": "2024-01-01T00:05:05"
      }
    ],
    "total": 10,
    "page": 1,
    "size": 50,
    "pages": 1
  },
  "timestamp": 1634567890123
}
```

### 发送消息

**接口地址**: `POST /api/v1/sessions/{sessionId}/messages`

**控制器**: `MessageController.sendMessage()`

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "content": "我今天心情很不好"
}
```

**参数验证**:
- `content`: 必填

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "userMessage": {
      "id": 2,
      "role": "user",
      "content": "我今天心情很不好",
      "msgType": "text",
      "createdAt": "2024-01-01T00:05:00"
    },
    "assistantMessage": {
      "id": 3,
      "role": "assistant",
      "content": "我理解你现在的心情...",
      "msgType": "text",
      "emotion": "悲伤",
      "tokenCount": 150,
      "createdAt": "2024-01-01T00:05:05"
    },
    "sessionTitle": "关于焦虑的对话",
    "referencedKnowledgeBases": ["心理学知识库", "情绪管理知识库"]
  },
  "timestamp": 1634567890123
}
```

**说明**:
- **工作流执行**：如果智能体关联了工作流，会执行工作流生成回复
- **RAG 检索**：如果智能体绑定了知识库（Agent层KB_a），会自动进行向量检索，将检索结果添加到系统提示词中
- **知识库引用**：返回引用的知识库名称列表（`referencedKnowledgeBases`），供前端展示
- **直接 LLM 调用**：如果智能体未关联工作流，直接调用 Agent 层的 LLM_a 生成回复
- **历史上下文**：自动获取最近10条消息用于上下文
- **自动标题生成**：首次对话后（消息数=3时，开场白1条+用户1条+助手1条），系统会根据用户第一条消息自动生成会话标题
- **热度统计**：仅 `session_type='normal'` 的会话计入智能体热度，`debug` 会话不计入
- **错误处理**：LLM 调用失败时返回友好错误信息

---

## 🏥 健康检查接口

### 健康检查

**接口地址**: `GET /api/v1/health`

**控制器**: `HealthController.health()`

**响应示例**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "status": "UP",
    "service": "SoulMate API",
    "version": "1.0.0",
    "time": "2024-01-01T00:00:00"
  },
  "timestamp": 1634567890123
}
```

### Ping接口

**接口地址**: `GET /api/ping`

**控制器**: `HealthController.ping()`

**响应**: `pong` (纯文本)

---

## 🔍 RAG服务接口

RAG服务运行在独立的Python服务中，提供向量化和检索功能。

### 文本向量化

**接口地址**: `POST /api/rag/embed`

**请求参数**:
```json
{
  "text": "这是要向量化的文本"
}
```

或批量模式：
```json
{
  "texts": ["文本1", "文本2", "文本3"]
}
```

**响应示例**:
```json
{
  "embedding": [0.1, 0.2, 0.3, ...]
}
```

或批量模式：
```json
{
  "embeddings": [[0.1, 0.2, ...], [0.3, 0.4, ...]]
}
```

### 索引文档切片

**接口地址**: `POST /api/rag/index`

**请求参数**:
```json
{
  "kb_id": "kb_psychology",
  "doc_id": 1,
  "segments": [
    "这是第一个文本片段",
    "这是第二个文本片段"
  ]
}
```

**响应示例**:
```json
{
  "success": true,
  "count": 2,
  "message": "成功索引 2 个片段到知识库 kb_psychology"
}
```

### 向量检索

**接口地址**: `POST /api/rag/search`

**请求参数**:
```json
{
  "kb_id": "kb_psychology",
  "query": "如何缓解焦虑情绪",
  "top_k": 3
}
```

**响应示例**:
```json
{
  "results": [
    {
      "text": "缓解焦虑的方法包括...",
      "score": 0.85,
      "doc_id": 1,
      "segment_id": "seg_123",
      "metadata": {}
    }
  ],
  "query": "如何缓解焦虑情绪",
  "kb_id": "kb_psychology",
  "total": 1
}
```

### 删除知识库集合

**接口地址**: `DELETE /api/rag/collection/{kb_id}`

**响应示例**:
```json
{
  "success": true,
  "message": "成功删除知识库 kb_psychology 的向量集合",
  "count": 0
}
```

### 删除文档向量

**接口地址**: `DELETE /api/rag/document/{kb_id}/{doc_id}`

**响应示例**:
```json
{
  "success": true,
  "message": "成功删除文档 1 的向量数据",
  "count": 10
}
```

### 获取集合统计信息

**接口地址**: `GET /api/rag/collection/{kb_id}/stats`

**响应示例**:
```json
{
  "kb_id": "kb_psychology",
  "document_count": 5,
  "segment_count": 100,
  "exists": true
}
```

### 健康检查

**接口地址**: `GET /api/rag/health`

**响应示例**:
```json
{
  "status": "healthy",
  "service": "RAG Service",
  "version": "1.0.0"
}
```

---

## 📋 错误码表

| 错误码 | HTTP状态码 | 说明 | 前端提示 |
|-------|-----------|------|---------|
| 0 | 200 | 成功 | - |
| 1001 | 400 | 请求参数错误 | "请求参数有误，请检查后重试" |
| 1002 | 500 | 系统错误 | "服务器异常，请稍后重试" |
| 2001 | 401 | 未授权（未登录或Token过期） | "登录已过期，请重新登录" |
| 2002 | 401 | 无效的Token | "登录已过期，请重新登录" |
| 2003 | 403 | 无权访问 | "您没有权限执行此操作" |
| 3001 | 404 | 用户不存在 | "用户不存在" |
| 3002 | 400 | 用户名已存在 | "用户名已被使用，请更换" |
| 3003 | 400 | 密码错误 | "密码错误，请重新输入" |
| 3004 | 403 | 账号已被禁用 | "账号已被禁用" |
| 4001 | 404 | 智能体不存在 | "智能体不存在" |
| 4002 | 400 | 智能体已下架 | "智能体已下架" |
| 5001 | 404 | 知识库不存在 | "知识库不存在" |
| 5002 | 404 | 文档不存在 | "文档不存在" |
| 5003 | 400 | 文档正在处理中 | "文档正在处理中，请稍候" |
| 5004 | 400 | 文件上传失败 | "文件上传失败，请重试" |
| 5005 | 400 | 参数无效 | "参数无效" |
| 5006 | 400 | 无效操作 | "无效操作" |
| 5007 | 500 | RAG服务调用失败 | "RAG服务调用失败，请稍后重试" |
| 6001 | 404 | 工作流不存在 | "工作流不存在" |
| 6002 | 400 | 工作流已禁用 | "工作流已禁用" |
| 7001 | 404 | 会话不存在 | "会话不存在" |
| 7002 | 403 | 无权访问该会话 | "无权访问该会话" |
| 8001 | 404 | LLM提供商不存在 | "LLM提供商不存在" |
| 8002 | 404 | LLM模型不存在 | "LLM模型不存在" |
| 8003 | 500 | LLM调用失败 | "AI服务暂时不可用，请稍后重试" |
| 9001 | 404 | 敏感词不存在 | "敏感词不存在" |
| 9002 | 400 | 敏感词已存在 | "敏感词已存在" |

**错误处理说明**:
- 前端应根据错误码显示对应的友好提示（见"前端提示"列）
- 401错误应自动跳转到登录页
- 网络错误应显示"网络连接失败，请检查网络"
- 请求超时应显示"请求超时，请稍后重试"

---

## 🔐 接口权限说明

| 接口路径 | 需要登录 | 需要Admin | 需要SuperAdmin | 说明 |
|---------|---------|-----------|---------------|------|
| `/api/v1/auth/*` | ❌ | ❌ | ❌ | 公开接口 |
| `/api/v1/profile/*` | ✅ | ❌ | ❌ | 个人中心 |
| `/api/v1/agents` (GET) | ❌ | ❌ | ❌ | 广场公开 |
| `/api/v1/agents` (POST/PUT/DELETE) | ✅ | ✅ | ✅ | Admin/SuperAdmin管理 |
| `/api/v1/knowledge-bases/*` | ✅ | ✅ | ✅ | Admin/SuperAdmin专用 |
| `/api/v1/workflows/*` | ✅ | ✅ | ✅ | Admin/SuperAdmin专用 |
| `/api/v1/llm/*` | ✅ | ✅ | ✅ | Admin/SuperAdmin专用 |
| `/api/v1/sensitive-words/*` | ✅ | ✅ | ✅ | Admin/SuperAdmin专用 |
| `/api/v1/sessions/*` | ✅ | ❌ | ❌ | 用户会话 |
| `/api/v1/users/*` | ✅ | ❌ | ✅ | SuperAdmin用户管理（仅SuperAdmin可访问） |

---

## 📝 注意事项

1. **Token过期**：Token默认24小时过期，过期后需重新登录
2. **文件上传**：文档上传需使用 `multipart/form-data` 格式，最大50MB
3. **分页限制**：单页最大数量限制为100
4. **并发限制**：消息发送接口建议前端做防抖处理，避免重复请求
5. **工作流执行**：工作流执行可能耗时较长（3-10秒），建议前端显示加载状态
6. **知识库处理**：文档上传后异步处理，需轮询文档状态接口（`GET /api/v1/knowledge-bases/{kbId}/documents/{docId}/status`）查询处理进度，建议每3秒查询一次，直到状态为 `completed` 或 `failed`
7. **工作流节点类型**：支持7种节点类型：start、text_process、safety_check、rag_retrieval、llm_process、crisis_intervention、end
8. **RAG服务**：RAG服务运行在独立端口，通过HTTP调用，需要确保RAG服务正常运行

---

## 🔄 接口版本说明

- 当前版本：`v1`
- 版本号通过URL路径指定：`/api/v1/...`
- 未来如需升级，新增 `/api/v2/...` 路径，保持向后兼容
