-- ============================================
-- SoulMate 数据库表结构
-- 基于 MySQL 8.0
-- 注意：此脚本会在 MySQL 容器首次启动时执行
-- 数据库名称由 docker-compose.yml 中的 MYSQL_DATABASE 环境变量决定
-- 开发环境: soulmate_db_dev
-- 生产环境: soulmate_db
-- ============================================

-- 设置字符集确保中文正确存储
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- MySQL 容器会自动切换到 MYSQL_DATABASE 指定的数据库，无需手动 USE

-- ============================================
-- 1. System 域 - 用户与安全
-- ============================================

-- 1.1 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id              VARCHAR(32)     NOT NULL COMMENT '用户ID (usr_xxx)',
    username        VARCHAR(50)     NOT NULL COMMENT '登录账号',
    password        VARCHAR(100)    NOT NULL COMMENT '密码 (BCrypt加密)',
    role            VARCHAR(20)     NOT NULL DEFAULT 'user' COMMENT '角色: user-普通用户, admin-管理员, superadmin-超级管理员',
    nickname        VARCHAR(50)     NULL COMMENT '昵称',
    avatar          VARCHAR(255)    NULL COMMENT '头像URL',
    bio             VARCHAR(200)    NULL COMMENT '个人简介',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '状态: 1-正常, 0-禁用',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_role_status (role, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 1.2 敏感词表
CREATE TABLE IF NOT EXISTS sys_sensitive_word (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    word            VARCHAR(50)     NOT NULL COMMENT '敏感词',
    category        VARCHAR(20)     NOT NULL DEFAULT 'general' COMMENT '分类: general-通用, crisis-危机干预, prohibited-禁止词',
    action          VARCHAR(20)     NOT NULL DEFAULT 'block' COMMENT '处理动作: block-拦截, warn-警告, replace-替换, intervention-干预',
    replacement     VARCHAR(50)     NULL COMMENT '替换文本 (action=replace时使用)',
    is_active       TINYINT         NOT NULL DEFAULT 1 COMMENT '是否启用: 1-是, 0-否',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_word (word),
    KEY idx_category_active (category, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词表';

-- ============================================
-- 2. LLM 域 - 大模型管理
-- ============================================

-- 2.1 LLM 提供商表
CREATE TABLE IF NOT EXISTS llm_provider (
    id              VARCHAR(32)     NOT NULL COMMENT '提供商ID (prv_xxx)',
    name            VARCHAR(50)     NOT NULL COMMENT '提供商名称',
    api_base        VARCHAR(255)    NULL COMMENT 'API 基础地址',
    api_key         VARCHAR(255)    NULL COMMENT 'API 密钥 (加密存储)',
    is_active       TINYINT         NOT NULL DEFAULT 1 COMMENT '是否启用: 1-是, 0-否',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LLM提供商表';

-- 2.2 LLM 模型表
CREATE TABLE IF NOT EXISTS llm_model (
    id              VARCHAR(32)     NOT NULL COMMENT '模型ID (mdl_xxx)',
    provider_id     VARCHAR(32)     NOT NULL COMMENT '所属提供商ID',
    name            VARCHAR(50)     NOT NULL COMMENT '模型名称 (如 deepseek-chat)',
    display_name    VARCHAR(50)     NOT NULL COMMENT '显示名称 (如 DeepSeek Chat)',
    model_type      VARCHAR(20)     NOT NULL DEFAULT 'chat' COMMENT '模型类型: chat-对话, embedding-向量',
    api_base        VARCHAR(255)    NULL COMMENT 'API 地址 (为空则使用提供商默认)',
    api_key         VARCHAR(255)    NULL COMMENT 'API 密钥 (为空则使用提供商默认)',
    default_config  JSON            NULL COMMENT '默认配置参数',
    is_active       TINYINT         NOT NULL DEFAULT 1 COMMENT '是否启用: 1-是, 0-否',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_provider (provider_id),
    KEY idx_type_active (model_type, is_active),
    CONSTRAINT fk_model_provider FOREIGN KEY (provider_id) REFERENCES llm_provider(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LLM模型表';

-- ============================================
-- 3. Knowledge 域 - 知识库管理
-- ============================================

-- 3.1 知识库表
CREATE TABLE IF NOT EXISTS ai_knowledge_base (
    id              VARCHAR(32)     NOT NULL COMMENT '知识库ID (kb_xxx)',
    name            VARCHAR(50)     NOT NULL COMMENT '知识库名称',
    description     VARCHAR(200)    NULL COMMENT '描述',
    embedding_model VARCHAR(32)     NULL COMMENT '使用的向量模型ID',
    doc_count       INT             NOT NULL DEFAULT 0 COMMENT '文档数量',
    segment_count   INT             NOT NULL DEFAULT 0 COMMENT '切片数量',
    is_active       INT             NOT NULL DEFAULT 1 COMMENT '是否启用: 1-是, 0-否',
    created_by      VARCHAR(32)     NOT NULL COMMENT '创建者ID',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_created_by (created_by),
    CONSTRAINT fk_kb_creator FOREIGN KEY (created_by) REFERENCES sys_user(id) ON DELETE RESTRICT,
    CONSTRAINT fk_kb_embedding FOREIGN KEY (embedding_model) REFERENCES llm_model(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库表';

-- 3.2 知识库文档表
CREATE TABLE IF NOT EXISTS ai_knowledge_document (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '文档ID',
    kb_id           VARCHAR(32)     NOT NULL COMMENT '所属知识库ID',
    file_name       VARCHAR(100)    NOT NULL COMMENT '文件名',
    file_type       VARCHAR(10)     NOT NULL COMMENT '文件类型: txt, md, pdf',
    file_size       INT             NOT NULL COMMENT '文件大小 (字节)',
    file_path       VARCHAR(255)    NOT NULL COMMENT '文件存储路径',
    segment_count   INT             NOT NULL DEFAULT 0 COMMENT '切片数量',
    status          VARCHAR(20)     NOT NULL DEFAULT 'pending' COMMENT '状态: pending-待处理, processing-处理中, completed-已完成, failed-失败',
    retry_count     INT             NOT NULL DEFAULT 0 COMMENT '重试次数（最大3次）',
    error_message   TEXT            NULL DEFAULT NULL COMMENT '错误信息（失败时的错误详情）',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_kb_id (kb_id),
    CONSTRAINT fk_doc_kb FOREIGN KEY (kb_id) REFERENCES ai_knowledge_base(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库文档表';

-- 3.3 知识切片表
CREATE TABLE IF NOT EXISTS ai_knowledge_segment (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '切片ID',
    kb_id           VARCHAR(32)     NOT NULL COMMENT '所属知识库ID',
    doc_id          BIGINT          NOT NULL COMMENT '来源文档ID',
    content         TEXT            NOT NULL COMMENT '切片文本内容',
    vector_id       VARCHAR(64)     NULL COMMENT 'ChromaDB 向量ID',
    word_count      INT             NOT NULL DEFAULT 0 COMMENT '字数',
    position        INT             NOT NULL DEFAULT 0 COMMENT '在文档中的位置序号',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '状态: 1-有效, 0-已删除',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_kb_status (kb_id, status),
    KEY idx_doc_id (doc_id),
    CONSTRAINT fk_segment_kb FOREIGN KEY (kb_id) REFERENCES ai_knowledge_base(id) ON DELETE CASCADE,
    CONSTRAINT fk_segment_doc FOREIGN KEY (doc_id) REFERENCES ai_knowledge_document(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识切片表';

-- ============================================
-- 4. Agent 域 - 智能体与工作流
-- ============================================

-- 4.1 工作流表
CREATE TABLE IF NOT EXISTS ai_workflow (
    id                      VARCHAR(32)     NOT NULL COMMENT '工作流ID (wfl_xxx)',
    name                    VARCHAR(50)     NOT NULL COMMENT '工作流名称',
    description             VARCHAR(200)    NULL COMMENT '描述',
    nodes_config            JSON            NOT NULL COMMENT '节点与连线配置 (资源绑定在节点内)',
    
    -- 状态字段
    status                  VARCHAR(20)     NOT NULL DEFAULT 'draft' COMMENT '工作流状态: draft-草稿, published-已发布, archived-已归档',
    validation_status       VARCHAR(20)     NULL COMMENT '验证状态: valid-通过, invalid-未通过, warning-有警告',
    
    -- 元数据字段（用于快速查询和统计，提升性能）
    node_count              INT             NOT NULL DEFAULT 0 COMMENT '节点数量',
    node_types              JSON            NULL COMMENT '节点类型列表，如 ["start", "text_process", "safety_check", "rag_retrieval", "llm_process", "end"]',
    has_rag                 TINYINT         NOT NULL DEFAULT 0 COMMENT '是否包含RAG节点: 1-是, 0-否',
    has_crisis_intervention TINYINT         NOT NULL DEFAULT 0 COMMENT '是否包含安全检测节点（safety_check）: 1-是, 0-否',
    
    is_active               TINYINT         NOT NULL DEFAULT 0 COMMENT '是否启用: 1-是, 0-否（只有published状态才能启用）',
    created_by              VARCHAR(32)     NOT NULL COMMENT '创建者ID',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    PRIMARY KEY (id),
    KEY idx_created_by (created_by),
    KEY idx_status (status),
    KEY idx_is_active (is_active),
    KEY idx_has_rag (has_rag),
    KEY idx_has_crisis_intervention (has_crisis_intervention),
    CONSTRAINT fk_workflow_creator FOREIGN KEY (created_by) REFERENCES sys_user(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流表';

-- 4.2 智能体表
CREATE TABLE IF NOT EXISTS ai_agent (
    id              VARCHAR(32)     NOT NULL COMMENT '智能体ID (agt_xxx)',
    name            VARCHAR(50)     NOT NULL COMMENT '智能体名称',
    avatar          VARCHAR(255)    NULL COMMENT '头像URL',
    description     VARCHAR(500)    NULL COMMENT '简介 (展示在广场)',
    tags            VARCHAR(100)    NULL COMMENT '标签 (逗号分隔，如: 情感,职场,压力)',
    greeting        VARCHAR(500)    NOT NULL DEFAULT '你好，我是你的心理陪伴伙伴，有什么想和我聊聊的吗？' COMMENT '开场白',
    system_prompt   TEXT            NOT NULL COMMENT '系统提示词 (人设)',
    model_id        VARCHAR(32)     NOT NULL COMMENT 'Agent 层 LLM_a 模型ID',
    model_config    JSON            NULL COMMENT 'LLM_a 参数覆盖配置',
    workflow_id     VARCHAR(32)     NULL COMMENT '关联的工作流ID（一个智能体只能关联一个工作流，可为空表示不使用工作流）',
    status          VARCHAR(10)     NOT NULL DEFAULT 'offline' COMMENT '状态: published-已上架(启用), offline-已下架。注意：智能体只有启用和下架两种状态，没有草稿状态，草稿状态等同于下架',
    heat_value      INT             NOT NULL DEFAULT 0 COMMENT '热度值 (对话次数)',
    created_by      VARCHAR(32)     NOT NULL COMMENT '创建者ID',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    published_at    DATETIME        NULL COMMENT '上架时间',
    PRIMARY KEY (id),
    KEY idx_status_heat (status, heat_value DESC),
    KEY idx_created_by (created_by),
    CONSTRAINT fk_agent_model FOREIGN KEY (model_id) REFERENCES llm_model(id) ON DELETE RESTRICT,
    CONSTRAINT fk_agent_workflow FOREIGN KEY (workflow_id) REFERENCES ai_workflow(id) ON DELETE SET NULL,
    CONSTRAINT fk_agent_creator FOREIGN KEY (created_by) REFERENCES sys_user(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能体表';

-- 4.3 智能体-知识库关联表
CREATE TABLE IF NOT EXISTS ai_agent_knowledge (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    agent_id        VARCHAR(32)     NOT NULL COMMENT '智能体ID',
    kb_id           VARCHAR(32)     NOT NULL COMMENT '知识库ID',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_agent_kb (agent_id, kb_id),
    KEY idx_kb_id (kb_id),
    CONSTRAINT fk_agent_kb_agent FOREIGN KEY (agent_id) REFERENCES ai_agent(id) ON DELETE CASCADE,
    CONSTRAINT fk_agent_kb_kb FOREIGN KEY (kb_id) REFERENCES ai_knowledge_base(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能体-知识库关联表';

-- ============================================
-- 5. Chat 域 - 会话与消息
-- ============================================

-- 5.1 会话表
CREATE TABLE IF NOT EXISTS chat_session (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    user_id         VARCHAR(32)     NOT NULL COMMENT '用户ID',
    agent_id        VARCHAR(32)     NOT NULL COMMENT '智能体ID',
    title           VARCHAR(50)     NOT NULL DEFAULT '新对话' COMMENT '会话标题',
    session_type    VARCHAR(10)     NOT NULL DEFAULT 'normal' COMMENT '会话类型: normal-普通, debug-调试',
    is_pinned       TINYINT         NOT NULL DEFAULT 0 COMMENT '是否置顶: 1-是, 0-否',
    message_count   INT             NOT NULL DEFAULT 0 COMMENT '消息数量',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后活跃时间',
    PRIMARY KEY (id),
    KEY idx_user_agent (user_id, agent_id),
    KEY idx_user_pinned_time (user_id, is_pinned DESC, updated_at DESC),
    CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_session_agent FOREIGN KEY (agent_id) REFERENCES ai_agent(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表';

-- 5.2 消息表
CREATE TABLE IF NOT EXISTS chat_message (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    session_id      BIGINT          NOT NULL COMMENT '会话ID',
    role            VARCHAR(10)     NOT NULL COMMENT '角色: user-用户, assistant-助手, system-系统',
    content         TEXT            NOT NULL COMMENT '消息内容',
    msg_type        VARCHAR(20)     NOT NULL DEFAULT 'text' COMMENT '消息类型: text-文本, greeting-开场白, crisis_alert-危机干预',
    emotion         VARCHAR(20)     NULL COMMENT '识别的情绪标签',
    token_count     INT             NULL COMMENT 'Token 消耗数',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_session_time (session_id, created_at),
    CONSTRAINT fk_message_session FOREIGN KEY (session_id) REFERENCES chat_session(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- ============================================
-- 6. Workflow 执行历史 - 调试与监控
-- ============================================

-- 6.1 工作流执行历史表
CREATE TABLE IF NOT EXISTS ai_workflow_execution (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    workflow_id     VARCHAR(32)     NOT NULL COMMENT '工作流ID',
    session_id      BIGINT          NULL COMMENT '会话ID',
    user_id         VARCHAR(32)     NULL COMMENT '用户ID',
    status          VARCHAR(20)     NOT NULL COMMENT '执行状态: success-成功, failed-失败, partial-部分成功',
    start_time      DATETIME        NOT NULL COMMENT '开始时间',
    end_time        DATETIME        NULL COMMENT '结束时间',
    duration_ms     INT             NULL COMMENT '执行耗时（毫秒）',
    input_data      JSON            NULL COMMENT '输入数据（用户输入、上下文等）',
    output_data     JSON            NULL COMMENT '输出数据（回复内容、情绪等）',
    node_executions JSON            NULL COMMENT '各节点执行结果',
    error_message   TEXT            NULL COMMENT '错误信息',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_workflow_id (workflow_id),
    KEY idx_session_id (session_id),
    KEY idx_user_id (user_id),
    KEY idx_created_at (created_at),
    CONSTRAINT fk_execution_workflow FOREIGN KEY (workflow_id) REFERENCES ai_workflow(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流执行历史表';
