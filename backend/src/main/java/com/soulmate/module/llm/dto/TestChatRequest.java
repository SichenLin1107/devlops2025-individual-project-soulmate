package com.soulmate.module.llm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 测试对话请求
 */
@Data
public class TestChatRequest {
    
    @NotBlank(message = "提供商ID不能为空")
    private String providerId;
    
    @NotBlank(message = "模型名称不能为空")
    private String modelName;
    
    private String apiBase;
    private String apiKey;
    
    @NotNull(message = "配置参数不能为空")
    private Map<String, Object> config;
    
    @NotBlank(message = "测试消息不能为空")
    private String message;
}
