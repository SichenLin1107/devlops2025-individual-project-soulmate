package com.soulmate.module.agent.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 智能体实体类
 */
@Data
public class AiAgent {
    
    private String id;
    private String name;
    private String avatar;
    private String description;
    private String tags;
    private String greeting;
    private String systemPrompt;
    private String modelId;
    private String modelConfig;
    private String workflowId;
    private String status;
    private Integer heatValue;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
}

