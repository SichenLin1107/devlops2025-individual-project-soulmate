package com.soulmate.module.knowledge.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识切片实体类
 */
@Data
public class AiKnowledgeSegment {
    
    private Long id;
    private String kbId;
    private Long docId;
    private String content;
    private String vectorId;
    private Integer wordCount;
    private Integer position;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

