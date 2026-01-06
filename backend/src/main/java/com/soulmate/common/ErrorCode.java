package com.soulmate.common;

import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
public enum ErrorCode {
    
    // 通用错误 (1xxx)
    SUCCESS(0, "操作成功"),
    PARAM_ERROR(1001, "参数错误"),
    SYSTEM_ERROR(1002, "系统错误"),
    
    // 认证错误 (2xxx)
    UNAUTHORIZED(2001, "未登录或Token已过期"),
    INVALID_TOKEN(2002, "无效的Token"),
    ACCESS_DENIED(2003, "无权访问"),
    
    // 用户错误 (3xxx)
    USER_NOT_FOUND(3001, "用户不存在"),
    USER_EXISTS(3002, "用户名已存在"),
    PASSWORD_ERROR(3003, "密码错误"),
    USER_DISABLED(3004, "账号已被禁用"),
    
    // 智能体错误 (4xxx)
    AGENT_NOT_FOUND(4001, "智能体不存在"),
    AGENT_OFFLINE(4002, "智能体已下架"),
    
    // 知识库错误 (5xxx)
    KNOWLEDGE_BASE_NOT_FOUND(5001, "知识库不存在"),
    DOCUMENT_NOT_FOUND(5002, "文档不存在"),
    DOCUMENT_PROCESSING(5003, "文档正在处理中"),
    FILE_UPLOAD_ERROR(5004, "文件上传失败"),
    INVALID_PARAMETER(5005, "参数无效"),
    INVALID_OPERATION(5006, "无效操作"),
    RAG_SERVICE_ERROR(5007, "RAG服务调用失败"),
    RAG_SERVICE_UNAVAILABLE(5008, "RAG服务不可用"),
    RAG_API_AUTH_ERROR(5009, "Embedding API认证失败，请检查API Key配置"),
    RAG_API_QUOTA_ERROR(5010, "Embedding API额度不足"),
    RAG_API_RATE_LIMIT(5011, "Embedding API请求频率超限"),
    RAG_API_TIMEOUT(5012, "Embedding API调用超时"),
    RAG_EMBEDDING_ERROR(5013, "文本向量化失败"),
    
    // 工作流错误 (6xxx)
    WORKFLOW_NOT_FOUND(6001, "工作流不存在"),
    WORKFLOW_DISABLED(6002, "工作流已禁用"),
    
    // 会话错误 (7xxx)
    SESSION_NOT_FOUND(7001, "会话不存在"),
    SESSION_ACCESS_DENIED(7002, "无权访问该会话"),
    
    // LLM错误 (8xxx)
    LLM_PROVIDER_NOT_FOUND(8001, "LLM提供商不存在"),
    LLM_MODEL_NOT_FOUND(8002, "LLM模型不存在"),
    LLM_CALL_ERROR(8003, "LLM调用失败"),
    LLM_API_AUTH_ERROR(8004, "LLM API认证失败，请检查API Key配置"),
    LLM_API_QUOTA_ERROR(8005, "LLM API额度不足"),
    LLM_API_RATE_LIMIT(8006, "LLM API请求频率超限"),
    LLM_API_TIMEOUT(8007, "LLM API调用超时"),
    LLM_API_MODEL_ERROR(8008, "LLM模型不可用或不存在"),
    
    // 敏感词错误 (9xxx)
    SENSITIVE_WORD_NOT_FOUND(9001, "敏感词不存在"),
    SENSITIVE_WORD_EXISTS(9002, "敏感词已存在");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}

