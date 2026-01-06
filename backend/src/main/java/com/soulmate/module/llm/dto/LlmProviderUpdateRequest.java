package com.soulmate.module.llm.dto;

import lombok.Data;

/**
 * LLM提供商更新请求
 */
@Data
public class LlmProviderUpdateRequest {
    
    private String name;
    private String apiBase;
    private String apiKey;
    private Integer isActive;
}

