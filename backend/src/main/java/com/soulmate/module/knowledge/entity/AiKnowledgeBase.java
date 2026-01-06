package com.soulmate.module.knowledge.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库实体类
 */
@Data
public class AiKnowledgeBase {
    
    private String id;
    private String name;
    private String description;
    private String embeddingModel;
    private Integer docCount;
    private Integer segmentCount;
    private Integer isActive;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

