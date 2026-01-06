package com.soulmate.module.llm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * LLM模型创建/更新请求
 */
@Data
public class LlmModelRequest {
    
    @NotBlank(message = "提供商ID不能为空")
    private String providerId;
    
    @NotBlank(message = "模型名称不能为空")
    private String name;
    
    @NotBlank(message = "显示名称不能为空")
    private String displayName;
    
    @NotBlank(message = "模型类型不能为空")
    private String modelType;
    
    private String apiBase;
    private String apiKey;
    private Map<String, Object> defaultConfig;
    private Integer isActive = 1;
    private Boolean disableRelatedAgents;
}

