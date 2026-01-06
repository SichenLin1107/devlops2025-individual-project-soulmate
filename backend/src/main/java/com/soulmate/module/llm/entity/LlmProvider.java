package com.soulmate.module.llm.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * LLM提供商实体类
 */
@Data
public class LlmProvider {
    
    private String id;
    private String name;
    private String apiBase;
    private String apiKey;
    private Integer isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

