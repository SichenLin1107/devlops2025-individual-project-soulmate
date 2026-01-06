package com.soulmate.module.llm.dto;

import lombok.Data;

import java.util.Map;

/**
 * LLM模型更新请求
 */
@Data
public class LlmModelUpdateRequest {
    
    private String name;
    private String displayName;
    private String modelType;
    private String apiBase;
    private String apiKey;
    private Map<String, Object> defaultConfig;
    private Integer isActive;
}

