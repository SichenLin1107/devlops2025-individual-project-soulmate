package com.soulmate.module.knowledge.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库文档实体类
 */
@Data
public class AiKnowledgeDocument {
    
    private Long id;
    private String kbId;
    private String fileName;
    private String fileType;
    private Integer fileSize;
    private String filePath;
    private Integer segmentCount;
    private String status;
    private Integer retryCount;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

