package com.soulmate.module.llm.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * LLM模型实体类
 */
@Data
public class LlmModel {
    
    private String id;
    private String providerId;
    private String name;
    private String displayName;
    private String modelType;
    private String apiBase;
    private String apiKey;
    private String defaultConfig;
    private Integer isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

