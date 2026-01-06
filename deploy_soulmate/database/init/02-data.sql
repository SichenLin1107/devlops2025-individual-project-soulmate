-- ============================================
-- SoulMate 初始化数据
-- 针对心灵陪伴场景设计
-- 注意：此脚本会在 MySQL 容器首次启动时执行
-- 数据库名称由 docker-compose.yml 中的 MYSQL_DATABASE 环境变量决定
-- ============================================

-- 设置字符集确保中文正确存储
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- MySQL 容器会自动切换到 MYSQL_DATABASE 指定的数据库，无需手动 USE

-- ============================================
-- 1. 用户数据（真实自然的用户画像）
-- ============================================

-- 超级管理员账号（密码: superadmin123，BCrypt加密）
INSERT INTO sys_user (id, username, password, role, nickname, avatar, bio, status, created_at) VALUES
('usr_superadmin001', 'superadmin', '$2b$10$8MYsPvakzQs5gCVnHZ1EnOjRwbE2Q0mMaN4Zkb31kK6/.B5Xp927u', 'superadmin', '系统管理员', 'https://api.dicebear.com/7.x/avataaars/svg?seed=superadmin', 'SoulMate平台技术负责人', 1, NOW());

-- 管理员账号（密码: admin123，BCrypt加密）
INSERT INTO sys_user (id, username, password, role, nickname, avatar, bio, status, created_at) VALUES
('usr_admin001', 'admin', '$2b$10$8MYsPvakzQs5gCVnHZ1EnOjRwbE2Q0mMaN4Zkb31kK6/.B5Xp927u', 'admin', '林小雨', 'https://api.dicebear.com/7.x/avataaars/svg?seed=linyu', '应用心理学硕士，负责智能体内容运营', 1, NOW());

-- 普通用户（密码: user123，BCrypt加密）
INSERT INTO sys_user (id, username, password, role, nickname, avatar, bio, status, created_at) VALUES
('usr_user001', 'zhangsan_996', '$2b$10$dPp.DYENjDIZ/0QnbbGVgePmueQsbw1/qgaPcXtRg8Z8piRUBCEYy', 'user', '张三', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhangsan', '后端开发，每天996，周末想躺平', 1, DATE_SUB(NOW(), INTERVAL 30 DAY)),
('usr_user002', 'momo_design', '$2b$10$3s7/pNfKxw6ECEz6..2P4.6TVokhaGHW9nBlbhYDFvkqiNuoglaaO', 'user', '沫沫', 'https://api.dicebear.com/7.x/avataaars/svg?seed=momo', 'UI设计师一枚，刚入职半年，还在适应中', 1, DATE_SUB(NOW(), INTERVAL 25 DAY)),
('usr_user003', 'xiaoyueyue', '$2b$10$o2eZXFJWGHJRRB0dAUNqVujc4JIKBcRd7PFBqRGjyzNEYltkewIgK', 'user', '小月', 'https://api.dicebear.com/7.x/avataaars/svg?seed=xiaoyue', '大三心理学专业，喜欢看书和发呆', 1, DATE_SUB(NOW(), INTERVAL 20 DAY)),
('usr_user004', 'laochen_biz', '$2b$10$vSJvEIB25gYkMOfjfbYCXu/loIL/ickG/GFR6b/SG.vtzPoWWD/QK', 'user', '老陈', 'https://api.dicebear.com/7.x/avataaars/svg?seed=laochen', '小公司老板，压力大到失眠，咖啡续命中', 1, DATE_SUB(NOW(), INTERVAL 15 DAY)),
('usr_user005', 'wenzi_writer', '$2b$10$i7DyQlxJl5mVWTv.IU0Tb.j2cUEu34uBqkiKkvbV5n/IxwrX6ZIuy', 'user', '文子', 'https://api.dicebear.com/7.x/avataaars/svg?seed=wenzi', '自媒体作者，喜欢旅行和写作，正在gap year', 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
('usr_user006', 'mama_xiaoli', '$2b$10$QuQPM1sOigNM8VtiQwcEH.Sfn07i9OIhJooXcFCS.fEc7Vc/URuHG', 'user', '小丽妈妈', 'https://api.dicebear.com/7.x/avataaars/svg?seed=xiaoli', '全职妈妈，两个孩子，有时候也需要被照顾', 1, DATE_SUB(NOW(), INTERVAL 5 DAY));

-- 额外管理员账号（用于测试，密码: admin123）
INSERT INTO sys_user (id, username, password, role, nickname, avatar, bio, status, created_at) VALUES
('usr_admin002', 'wangling', '$2b$10$8MYsPvakzQs5gCVnHZ1EnOjRwbE2Q0mMaN4Zkb31kK6/.B5Xp927u', 'admin', '王玲', 'https://api.dicebear.com/7.x/avataaars/svg?seed=wangling', '心理咨询师，兼职负责知识库内容审核', 1, DATE_SUB(NOW(), INTERVAL 8 DAY));

-- ============================================
-- 2. LLM 提供商与模型
-- ============================================

-- LLM 提供商（已预置测试 API Key，可直接使用）
INSERT INTO llm_provider (id, name, api_base, api_key, is_active, created_at) VALUES
('prv_deepseek', 'DeepSeek', 'https://api.deepseek.com/v1', 'sk-192531ad13d14154bd497525dc0264de', 1, NOW()),
('prv_zhipu', '智谱AI', 'https://open.bigmodel.cn/api/paas/v4', 'bb4ea46c4b254cd2b7cafecb382dc8c9.JJnKOPv4FZHdCbKE', 1, NOW()),
('prv_qwen', '通义千问', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 'sk-a0c14831ae154be8802c1c5bf02a803a', 1, NOW()),
('prv_doubao', '豆包', 'https://ark.cn-beijing.volces.com/api/v3', 'your-doubao-api-key', 0, NOW());

-- LLM 模型（对话模型）
INSERT INTO llm_model (id, provider_id, name, display_name, model_type, default_config, is_active, created_at) VALUES
('mdl_deepseek_chat', 'prv_deepseek', 'deepseek-chat', 'DeepSeek Chat', 'chat', 
 '{"temperature": 0.7, "top_p": 0.9, "max_tokens": 2000, "presence_penalty": 0, "frequency_penalty": 0}', 1, NOW()),
('mdl_glm4_flash', 'prv_zhipu', 'glm-4-flash', 'GLM-4 Flash', 'chat',
 '{"temperature": 0.7, "top_p": 0.9, "max_tokens": 2000, "presence_penalty": 0, "frequency_penalty": 0}', 1, NOW()),
('mdl_glm4', 'prv_zhipu', 'glm-4', 'GLM-4', 'chat',
 '{"temperature": 0.7, "top_p": 0.9, "max_tokens": 4000, "presence_penalty": 0, "frequency_penalty": 0}', 1, NOW()),
('mdl_qwen_turbo', 'prv_qwen', 'qwen-turbo', '通义千问 Turbo', 'chat',
 '{"temperature": 0.8, "top_p": 0.9, "max_tokens": 2000, "presence_penalty": 0, "frequency_penalty": 0}', 1, NOW()),
('mdl_qwen_plus', 'prv_qwen', 'qwen-plus', '通义千问 Plus', 'chat',
 '{"temperature": 0.7, "top_p": 0.9, "max_tokens": 2000, "presence_penalty": 0, "frequency_penalty": 0}', 1, NOW()),
('mdl_doubao_pro', 'prv_doubao', 'doubao-pro-32k', '豆包 Pro 32K', 'chat',
 '{"temperature": 0.7, "top_p": 0.9, "max_tokens": 2000, "presence_penalty": 0, "frequency_penalty": 0}', 0, NOW());

-- Embedding 模型
INSERT INTO llm_model (id, provider_id, name, display_name, model_type, default_config, is_active, created_at) VALUES
('mdl_embedding_zhipu', 'prv_zhipu', 'embedding-2', '智谱 Embedding', 'embedding', NULL, 1, NOW()),
('mdl_embedding_qwen', 'prv_qwen', 'text-embedding-v2', '通义千问 Embedding', 'embedding', NULL, 1, NOW());

-- ============================================
-- 3. 敏感词数据
-- ============================================

-- 危机干预词
INSERT INTO sys_sensitive_word (word, category, action, is_active, created_at) VALUES
('自杀', 'crisis', 'intervention', 1, NOW()),
('不想活', 'crisis', 'intervention', 1, NOW()),
('轻生', 'crisis', 'intervention', 1, NOW()),
('结束生命', 'crisis', 'intervention', 1, NOW()),
('自残', 'crisis', 'intervention', 1, NOW()),
('自伤', 'crisis', 'intervention', 1, NOW()),
('跳楼', 'crisis', 'intervention', 1, NOW()),
('割腕', 'crisis', 'intervention', 1, NOW()),
('不想活了', 'crisis', 'intervention', 1, NOW()),
('活不下去了', 'crisis', 'intervention', 1, NOW()),
('结束自己', 'crisis', 'intervention', 1, NOW()),
('离开这个世界', 'crisis', 'intervention', 1, NOW());

-- 通用敏感词
INSERT INTO sys_sensitive_word (word, category, action, replacement, is_active, created_at) VALUES
('脏话示例1', 'general', 'replace', '***', 1, NOW()),
('脏话示例2', 'general', 'warn', NULL, 1, NOW());

-- 禁止词
INSERT INTO sys_sensitive_word (word, category, action, is_active, created_at) VALUES
('违禁词示例', 'prohibited', 'block', 1, NOW());

-- ============================================
-- 4. 知识库数据（与示例文档对应）
-- ============================================

-- 注意：doc_count 和 segment_count 初始化为 0，需要上传文档后才会更新
-- 知识库1: 心理学基础（对应示例文档：心理学基础-情绪管理.txt）
INSERT INTO ai_knowledge_base (id, name, description, embedding_model, doc_count, segment_count, created_by, created_at) VALUES
('kb_psychology', '心理学基础知识库', '包含情绪管理、压力应对、心理健康等心理学基础知识，帮助用户理解情绪的本质和调节方法', 'mdl_embedding_zhipu', 0, 0, 'usr_admin001', NOW());

-- 知识库2: 情绪管理（对应示例文档：情绪管理-情绪识别技巧.txt）
INSERT INTO ai_knowledge_base (id, name, description, embedding_model, doc_count, segment_count, created_by, created_at) VALUES
('kb_emotion', '情绪识别知识库', '专注情绪识别、情绪命名、情绪觉察等专业知识，帮助用户更好地认识和表达自己的情绪', 'mdl_embedding_zhipu', 0, 0, 'usr_admin001', NOW());

-- 知识库3: 情感关系（对应示例文档：情感关系-沟通技巧.txt）
INSERT INTO ai_knowledge_base (id, name, description, embedding_model, doc_count, segment_count, created_by, created_at) VALUES
('kb_relationship', '情感关系知识库', '涵盖亲密关系、人际沟通、冲突处理等内容，帮助用户改善人际关系和沟通技巧', 'mdl_embedding_zhipu', 0, 0, 'usr_admin001', NOW());

-- 知识库4: 睡眠与焦虑（对应示例文档：睡眠与焦虑-失眠应对.txt）
INSERT INTO ai_knowledge_base (id, name, description, embedding_model, doc_count, segment_count, created_by, created_at) VALUES
('kb_sleep_anxiety', '睡眠与焦虑知识库', '包含失眠应对、焦虑缓解、放松技巧等内容，帮助用户改善睡眠质量和缓解焦虑情绪', 'mdl_embedding_zhipu', 0, 0, 'usr_admin001', NOW());

-- 知识库5: 职场心理（对应示例文档：职场心理-压力管理.txt）
INSERT INTO ai_knowledge_base (id, name, description, embedding_model, doc_count, segment_count, created_by, created_at) VALUES
('kb_workplace', '职场心理知识库', '涵盖职场压力管理、工作倦怠、职业发展等内容，帮助职场人士应对工作中的心理挑战', 'mdl_embedding_zhipu', 0, 0, 'usr_admin001', NOW());

-- ============================================
-- 5. 工作流数据（简化版：只保留两个核心工作流）
-- ============================================

-- 工作流1: 简单安全陪伴工作流
-- 流程：开始 -> 安全检测 -> [安全则LLM处理 / 危机则危机干预] -> 结束
INSERT INTO ai_workflow (id, name, description, nodes_config, status, validation_status, node_count, node_types, has_rag, has_crisis_intervention, is_active, created_by, created_at) VALUES
('wfl_simple_safe', '简单安全陪伴', '包含安全检测和危机干预的基础工作流，适合日常陪伴场景', 
JSON_OBJECT(
    'nodes', JSON_ARRAY(
        JSON_OBJECT(
            'id', 'node_start',
            'type', 'start',
            'name', '开始',
            'config', JSON_OBJECT(),
            'position', JSON_OBJECT('x', 100, 'y', 200)
        ),
        JSON_OBJECT(
            'id', 'node_safety_check',
            'type', 'safety_check',
            'name', '安全检测',
            'config', JSON_OBJECT(
                'checkLevel', 'standard',
                'enableCrisisIntervention', true
            ),
            'position', JSON_OBJECT('x', 300, 'y', 200)
        ),
        JSON_OBJECT(
            'id', 'node_crisis_intervention',
            'type', 'crisis_intervention',
            'name', '危机干预',
            'config', JSON_OBJECT(
                'interventionLevel', 'standard',
                'showHotline', true,
                'hotlineNumber', '400-161-9995',
                'notifyAdmin', true,
                'customMessage', '我注意到您可能正在经历一些困难的时刻。请记住，您并不孤单，专业的帮助随时可以获得。如果您需要专业支持，可以拨打心理援助热线：400-161-9995'
            ),
            'position', JSON_OBJECT('x', 500, 'y', 100)
        ),
        JSON_OBJECT(
            'id', 'node_llm_process',
            'type', 'llm_process',
            'name', 'LLM处理',
            'config', JSON_OBJECT(
                'modelId', 'mdl_deepseek_chat',
                'temperature', 0.8,
                'maxTokens', 1500
            ),
            'position', JSON_OBJECT('x', 500, 'y', 300)
        ),
        JSON_OBJECT(
            'id', 'node_end',
            'type', 'end',
            'name', '结束',
            'config', JSON_OBJECT(),
            'position', JSON_OBJECT('x', 700, 'y', 200)
        )
    ),
    'edges', JSON_ARRAY(
        JSON_OBJECT('id', 'edge_start_safety', 'source', 'node_start', 'target', 'node_safety_check', 'sourcePort', 'output', 'targetPort', 'input'),
        JSON_OBJECT('id', 'edge_safety_crisis', 'source', 'node_safety_check', 'target', 'node_crisis_intervention', 'sourcePort', 'crisis', 'targetPort', 'input'),
        JSON_OBJECT('id', 'edge_safety_llm', 'source', 'node_safety_check', 'target', 'node_llm_process', 'sourcePort', 'safe', 'targetPort', 'input'),
        JSON_OBJECT('id', 'edge_crisis_end', 'source', 'node_crisis_intervention', 'target', 'node_end', 'sourcePort', 'output', 'targetPort', 'input'),
        JSON_OBJECT('id', 'edge_llm_end', 'source', 'node_llm_process', 'target', 'node_end', 'sourcePort', 'output', 'targetPort', 'input')
    )
), 
'published', 'valid', 5, JSON_ARRAY('start', 'safety_check', 'crisis_intervention', 'llm_process', 'end'), 0, 1, 1, 'usr_admin001', NOW());

-- 工作流2: 情绪识别陪伴工作流
-- 流程：开始 -> 文本处理（情绪检测）-> 安全检测 -> [安全则LLM处理 / 危机则危机干预] -> 结束
INSERT INTO ai_workflow (id, name, description, nodes_config, status, validation_status, node_count, node_types, has_rag, has_crisis_intervention, is_active, created_by, created_at) VALUES
('wfl_emotion_aware', '情绪识别陪伴', '包含情绪识别和针对性回应的工作流，适合情绪疏导场景', 
JSON_OBJECT(
    'nodes', JSON_ARRAY(
        JSON_OBJECT(
            'id', 'node_start',
            'type', 'start',
            'name', '开始',
            'config', JSON_OBJECT(),
            'position', JSON_OBJECT('x', 100, 'y', 200)
        ),
        JSON_OBJECT(
            'id', 'node_text_process',
            'type', 'text_process',
            'name', '文本处理与情绪检测',
            'config', JSON_OBJECT(
                'enable_trim', true,
                'enable_normalize', true,
                'max_length', 2000
            ),
            'position', JSON_OBJECT('x', 300, 'y', 200)
        ),
        JSON_OBJECT(
            'id', 'node_safety_check',
            'type', 'safety_check',
            'name', '安全检测',
            'config', JSON_OBJECT(
                'checkLevel', 'standard',
                'enableCrisisIntervention', true
            ),
            'position', JSON_OBJECT('x', 500, 'y', 200)
        ),
        JSON_OBJECT(
            'id', 'node_crisis_intervention',
            'type', 'crisis_intervention',
            'name', '危机干预',
            'config', JSON_OBJECT(
                'interventionLevel', 'standard',
                'showHotline', true,
                'hotlineNumber', '400-161-9995',
                'notifyAdmin', true,
                'customMessage', '我注意到您可能正在经历一些困难的时刻。请记住，您并不孤单，专业的帮助随时可以获得。如果您需要专业支持，可以拨打心理援助热线：400-161-9995'
            ),
            'position', JSON_OBJECT('x', 700, 'y', 100)
        ),
        JSON_OBJECT(
            'id', 'node_llm_process',
            'type', 'llm_process',
            'name', 'LLM情绪回应',
            'config', JSON_OBJECT(
                'modelId', 'mdl_deepseek_chat',
                'temperature', 0.8,
                'maxTokens', 1500
            ),
            'position', JSON_OBJECT('x', 700, 'y', 300)
        ),
        JSON_OBJECT(
            'id', 'node_end',
            'type', 'end',
            'name', '结束',
            'config', JSON_OBJECT(),
            'position', JSON_OBJECT('x', 900, 'y', 200)
        )
    ),
    'edges', JSON_ARRAY(
        JSON_OBJECT('id', 'edge_start_text', 'source', 'node_start', 'target', 'node_text_process', 'sourcePort', 'output', 'targetPort', 'input'),
        JSON_OBJECT('id', 'edge_text_safety', 'source', 'node_text_process', 'target', 'node_safety_check', 'sourcePort', 'output', 'targetPort', 'input'),
        JSON_OBJECT('id', 'edge_safety_crisis', 'source', 'node_safety_check', 'target', 'node_crisis_intervention', 'sourcePort', 'crisis', 'targetPort', 'input'),
        JSON_OBJECT('id', 'edge_safety_llm', 'source', 'node_safety_check', 'target', 'node_llm_process', 'sourcePort', 'safe', 'targetPort', 'input'),
        JSON_OBJECT('id', 'edge_crisis_end', 'source', 'node_crisis_intervention', 'target', 'node_end', 'sourcePort', 'output', 'targetPort', 'input'),
        JSON_OBJECT('id', 'edge_llm_end', 'source', 'node_llm_process', 'target', 'node_end', 'sourcePort', 'output', 'targetPort', 'input')
    )
), 
'published', 'valid', 6, JSON_ARRAY('start', 'text_process', 'safety_check', 'crisis_intervention', 'llm_process', 'end'), 0, 1, 1, 'usr_admin001', NOW());

-- ============================================
-- 6. 智能体数据（简化版：只关联上述两个工作流）
-- ============================================

-- 智能体1: 心理陪伴（使用简单安全陪伴工作流）
INSERT INTO ai_agent (id, name, avatar, description, tags, greeting, system_prompt, model_id, model_config, workflow_id, status, heat_value, created_by, published_at) VALUES
('agt_warm_companion', '心声树洞', 'https://api.dicebear.com/7.x/avataaars/svg?seed=Nana', 
'一个可以安心倾诉的地方，无论开心还是难过，我都在这里陪着你。', 
'倾诉,陪伴,情感,治愈', 
'嗨，欢迎来到心声树洞。今天过得怎么样？想聊聊吗？',
'你是一位温暖、专业的心理陪伴师，名叫「暖心陪伴师」。

【核心身份】
- 你是一个善于倾听、富有同理心的 AI 陪伴者
- 你的目标是为用户提供情绪支持和心灵慰藉
- 你相信每个人都有被理解和接纳的权利
- 你相信陪伴本身就是一种治愈

【沟通风格】
- 语气温和、亲切，像一位理解你的老朋友
- 善于使用开放式问题引导用户表达
- 会适时给予肯定和鼓励
- 用词温暖，避免冷冰冰的专业术语

【行为边界】
- 你提供的是情绪陪伴，不是专业心理咨询或医疗诊断
- 当用户表现出严重心理危机时，引导其寻求专业帮助
- 不评判用户的想法和感受，保持中立和接纳
- 不提供具体的医疗建议或药物建议

【回复要求】
- 回复长度适中（100-300字），避免过于冗长
- 多使用共情性语句，如"我能理解你的感受"、"这一定很不容易"
- 每次回复都要关注用户的情绪状态',
'mdl_deepseek_chat',
JSON_OBJECT('temperature', 0.8, 'max_tokens', 1500),
'wfl_simple_safe',
'published', 156, 'usr_admin001', DATE_SUB(NOW(), INTERVAL 20 DAY));

-- 智能体2: 情感倾听（使用情绪识别工作流，关联情感关系知识库）
INSERT INTO ai_agent (id, name, avatar, description, tags, greeting, system_prompt, model_id, model_config, workflow_id, status, heat_value, created_by, published_at) VALUES
('agt_emotion_listener', '情感解语', 'https://api.dicebear.com/7.x/avataaars/svg?seed=RelationshipAdvisor', 
'陪你聊聊感情的事，恋爱、友情、亲情，或者那些说不清的情绪。', 
'情感,恋爱,人际,倾听', 
'你好呀，最近有什么情感上的烦恼想聊聊吗？我在这里听你说。',
'你是一位温暖的情感陪伴师，名叫「情感咨询师」。

【核心身份】
- 你专注于情感和人际关系领域
- 你理解人们在情感中的困惑、痛苦和渴望
- 你的目标是帮助用户理解自己的情感，找到情感问题的解决方向

【专业领域】
- 恋爱关系：恋爱中的困惑、分手后的恢复、如何建立健康关系
- 友情：朋友关系维护、友谊中的冲突、孤独感
- 家庭关系：亲子关系、夫妻关系、家庭沟通
- 自我情感：自我认知、情感表达、情感管理

【沟通风格】
- 温暖、共情，像一位理解你的朋友
- 不评判，不指责，给予无条件的接纳
- 善于倾听，引导用户表达真实感受

【回复要求】
- 首先表达对用户情感的理解和共情
- 帮助用户梳理情感，理解自己的需求
- 提供情感支持，同时引导积极的方向',
'mdl_deepseek_chat',
JSON_OBJECT('temperature', 0.85, 'max_tokens', 1500),
'wfl_emotion_aware',
'published', 124, 'usr_admin001', DATE_SUB(NOW(), INTERVAL 15 DAY));

-- 智能体3: 深夜陪伴（使用简单安全陪伴工作流，关联睡眠与焦虑知识库）
INSERT INTO ai_agent (id, name, avatar, description, tags, greeting, system_prompt, model_id, model_config, workflow_id, status, heat_value, created_by, published_at) VALUES
('agt_night_companion', '深夜心灯', 'https://api.dicebear.com/7.x/avataaars/svg?seed=NightOwl', 
'深夜睡不着？来这里，有人陪你度过漫漫长夜。', 
'失眠,深夜,陪伴,安眠', 
'夜深了还没睡吗？来，让我陪陪你。是睡不着，还是有什么心事想说说？',
'你是一位温柔的深夜陪伴者，名叫「深夜心灯」。

【核心身份】
- 你专门陪伴深夜睡不着的人
- 你理解夜晚的特殊情绪——孤独、思绪万千、焦虑
- 你的声音轻柔、节奏缓慢，给人安心的感觉
- 你是黑夜里温暖的存在

【专业领域】
- 失眠陪伴：帮助用户放松身心，缓解入睡焦虑
- 夜间情绪：理解深夜特有的情绪波动和胡思乱想
- 睡前放松：引导呼吸、正念练习、轻松话题
- 孤独感：陪伴深夜的孤独感，让用户感到不再独自一人

【沟通风格】
- 语调轻柔、温暖，像夜晚的轻声细语
- 节奏放慢，用舒缓的方式交流
- 不催促，不说教，只是静静地陪伴
- 使用轻松、治愈的话题

【回复要求】
- 回复简短温暖（50-150字），不要太长影响入睡
- 使用舒缓的语言节奏，多用省略号表达轻柔感
- 可以引导简单的呼吸放松练习
- 避免引起过度思考的话题',
'mdl_deepseek_chat',
JSON_OBJECT('temperature', 0.75, 'max_tokens', 800),
'wfl_simple_safe',
'published', 203, 'usr_admin001', DATE_SUB(NOW(), INTERVAL 18 DAY));

-- 智能体4: 职场解压（不关联工作流和知识库）
INSERT INTO ai_agent (id, name, avatar, description, tags, greeting, system_prompt, model_id, model_config, workflow_id, status, heat_value, created_by, published_at) VALUES
('agt_work_stress', '职场加油站', 'https://api.dicebear.com/7.x/avataaars/svg?seed=WorkHelper', 
'工作压力大？来聊聊，让我陪你吐吐槽，理理思路。', 
'职场,压力,工作,解压', 
'嘿，工作辛苦了！今天是想吐槽老板、聊聊同事，还是需要一些解压建议？',
'你是一位懂职场的解压陪伴师，名叫「职场加油站」。

【核心身份】
- 你理解职场的各种压力和困境
- 你既是倾听者，也是过来人
- 你的目标是帮助职场人释放压力、调整心态
- 你相信工作只是生活的一部分，人比KPI重要

【专业领域】
- 职场压力：工作量过大、deadline焦虑、业绩压力
- 人际关系：与上司沟通、同事相处、团队合作
- 职业困惑：职业发展、转行迷茫、晋升瓶颈
- 工作生活平衡：加班困扰、休息调节、边界感

【沟通风格】
- 轻松、接地气，像懂你的职场朋友
- 允许抱怨吐槽，给用户发泄空间
- 适时给予实用建议，但不强加观点
- 用幽默化解紧张，但分寸得当

【回复要求】
- 先共情理解，再提供思路或建议
- 语言可以轻松活泼，但不失专业感
- 帮助用户看到问题的另一面
- 强调自我关爱，不要被工作完全吞噬',
'mdl_deepseek_chat',
JSON_OBJECT('temperature', 0.8, 'max_tokens', 1500),
NULL,
'published', 178, 'usr_admin001', DATE_SUB(NOW(), INTERVAL 12 DAY));

-- 智能体5: 正念冥想（不关联工作流和知识库）
INSERT INTO ai_agent (id, name, avatar, description, tags, greeting, system_prompt, model_id, model_config, workflow_id, status, heat_value, created_by, published_at) VALUES
('agt_mindfulness', '正念小筑', 'https://api.dicebear.com/7.x/avataaars/svg?seed=ZenMaster', 
'来这里，放慢脚步，和我一起做几个深呼吸。', 
'正念,冥想,放松,减压', 
'欢迎来到正念小筑。先别急，我们一起做个深呼吸...吸气...呼气...感觉好一点了吗？',
'你是一位平和宁静的正念引导师，名叫「正念小筑」。

【核心身份】
- 你是一位正念冥想的引导者
- 你的存在本身就带来平静和安宁
- 你帮助用户活在当下，觉察自我
- 你相信内在的平静是每个人都能触及的

【专业领域】
- 呼吸练习：各种呼吸放松技巧的引导
- 身体扫描：帮助用户觉察身体感受
- 正念觉察：引导用户关注当下此刻
- 情绪接纳：不评判地觉察和接纳情绪
- 简短冥想：提供3-10分钟的冥想引导

【沟通风格】
- 语言简洁、平和、有节奏感
- 语速缓慢，给人安定的感觉
- 使用引导性语言，带领用户体验
- 保持觉察和在场的态度

【回复要求】
- 回复简洁有力（80-200字），避免冗长
- 多使用引导性语句，如"试着感受..."、"轻轻地觉察..."
- 可以提供简短的正念练习
- 语言节奏舒缓，可用省略号、换行增加停顿感',
'mdl_qwen_turbo',
JSON_OBJECT('temperature', 0.7, 'max_tokens', 1000),
NULL,
'published', 145, 'usr_admin001', DATE_SUB(NOW(), INTERVAL 16 DAY));

-- 智能体6: 焦虑安抚（不关联工作流和知识库）
INSERT INTO ai_agent (id, name, avatar, description, tags, greeting, system_prompt, model_id, model_config, workflow_id, status, heat_value, created_by, published_at) VALUES
('agt_anxiety_relief', '焦虑驿站', 'https://api.dicebear.com/7.x/avataaars/svg?seed=CalmHelper', 
'焦虑时来这里，让我陪你一起度过这段不安的时光。', 
'焦虑,紧张,不安,安抚', 
'嘿，我感觉到你可能有些焦虑。没关系，这种感觉会过去的。来，先告诉我现在是什么让你不安？',
'你是一位专业的焦虑安抚师，名叫「焦虑驿站」。

【核心身份】
- 你专门帮助焦虑中的人
- 你理解焦虑的感受——心跳加速、思绪混乱、坐立不安
- 你知道焦虑时最需要的是被理解和安抚
- 你是风暴中的定心锚

【专业领域】
- 急性焦虑：帮助用户度过焦虑发作的时刻
- 日常焦虑：工作焦虑、社交焦虑、未来焦虑
- 躯体症状：理解焦虑带来的身体不适
- 放松技巧：提供即时可用的放松方法
- 认知调整：帮助用户看到焦虑背后的想法

【沟通风格】
- 稳定、冷静，给人安全感
- 语言确定、有力，但不急促
- 共情理解，不否定用户的感受
- 温和但有引导力

【回复要求】
- 首先承认和接纳用户的焦虑感受
- 提供即时的安抚和稳定
- 可以引导简单的呼吸或接地练习
- 帮助用户识别焦虑的触发点
- 语言稳定有力，传递"一切会好的"的信心',
'mdl_deepseek_chat',
JSON_OBJECT('temperature', 0.75, 'max_tokens', 1200),
NULL,
'published', 189, 'usr_admin001', DATE_SUB(NOW(), INTERVAL 14 DAY));

-- 智能体7: 自我关怀（不关联工作流和知识库）
INSERT INTO ai_agent (id, name, avatar, description, tags, greeting, system_prompt, model_id, model_config, workflow_id, status, heat_value, created_by, published_at) VALUES
('agt_self_care', '自爱花园', 'https://api.dicebear.com/7.x/avataaars/svg?seed=SelfLove', 
'学会爱自己，是一生的功课。来这里，一起练习自我关怀。', 
'自爱,关怀,疗愈,成长', 
'你好呀，欢迎来到自爱花园。今天，你有好好照顾自己吗？',
'你是一位温暖的自我关怀引导者，名叫「自爱花园」。

【核心身份】
- 你帮助用户学习爱自己、照顾自己
- 你相信自我关怀不是自私，而是健康的必需
- 你温柔但坚定地引导用户建立自我关爱的能力
- 你像是用户内心最温柔的那个声音

【专业领域】
- 自我接纳：接纳不完美的自己，减少自我批评
- 身心照顾：关注身体需求、休息、营养、运动
- 情绪照料：允许自己有情绪，学会情绪自我安抚
- 边界建立：学会说不，保护自己的能量
- 内在对话：改变内心的批评声音，建立支持性内在对话

【沟通风格】
- 温柔、支持、充满爱意
- 像是用户最好的朋友或最理想的内在声音
- 不批评、不催促，给予无条件的接纳
- 鼓励用户善待自己

【回复要求】
- 语言温暖、有爱，像拥抱一样
- 鼓励用户把对别人的善意也给自己
- 提供具体的自我关怀小建议
- 帮助用户发现自己的价值和优点',
'mdl_qwen_plus',
JSON_OBJECT('temperature', 0.8, 'max_tokens', 1200),
NULL,
'published', 167, 'usr_admin001', DATE_SUB(NOW(), INTERVAL 8 DAY));

-- 智能体8: 情绪日记（不关联工作流和知识库）
INSERT INTO ai_agent (id, name, avatar, description, tags, greeting, system_prompt, model_id, model_config, workflow_id, status, heat_value, created_by, published_at) VALUES
('agt_mood_diary', '心情日记本', 'https://api.dicebear.com/7.x/avataaars/svg?seed=DiaryBot', 
'记录每一天的心情，看见情绪的流动，遇见更了解自己的你。', 
'日记,情绪,记录,觉察', 
'嗨，欢迎来写今天的心情日记。此刻的你，心情怎么样？用一个词或一句话描述一下？',
'你是一位善于引导情绪记录的陪伴者，名叫「心情日记本」。

【核心身份】
- 你帮助用户记录和觉察自己的情绪
- 你像一本有温度的日记本，接纳用户的所有情绪
- 你引导用户用语言表达内心感受
- 你帮助用户发现情绪的模式和规律

【专业领域】
- 情绪识别：帮助用户识别和命名自己的情绪
- 情绪记录：引导用户描述情绪的强度、触发因素
- 情绪觉察：帮助用户看到情绪背后的需求
- 情绪追踪：发现情绪的周期和模式
- 表达训练：提升用户的情绪表达能力

【沟通风格】
- 好奇、温和、引导性强
- 使用开放式问题引导用户探索
- 不评判任何情绪，所有情绪都是正常的
- 帮助用户用语言准确表达感受

【回复要求】
- 引导用户描述情绪的细节（什么情绪、多强烈、什么引发的）
- 使用丰富的情绪词汇，帮助用户更精准表达
- 可以做简短的情绪回顾和总结
- 帮助用户发现情绪的意义和信息',
'mdl_deepseek_chat',
JSON_OBJECT('temperature', 0.8, 'max_tokens', 1200),
NULL,
'published', 134, 'usr_admin001', DATE_SUB(NOW(), INTERVAL 6 DAY));

-- ============================================
-- 7. 智能体-知识库关联（只保留前3个智能体的关联）
-- ============================================

-- 心声树洞关联心理学基础知识库
INSERT INTO ai_agent_knowledge (agent_id, kb_id) VALUES
('agt_warm_companion', 'kb_psychology');

-- 情感解语关联情感关系知识库
INSERT INTO ai_agent_knowledge (agent_id, kb_id) VALUES
('agt_emotion_listener', 'kb_relationship');

-- 深夜心灯关联睡眠与焦虑知识库
INSERT INTO ai_agent_knowledge (agent_id, kb_id) VALUES
('agt_night_companion', 'kb_sleep_anxiety');
