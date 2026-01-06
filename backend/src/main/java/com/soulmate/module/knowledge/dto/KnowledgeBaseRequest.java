package com.soulmate.module.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 知识库创建或更新请求
 */
@Data
public class KnowledgeBaseRequest {
    
    @NotBlank(message = "知识库名称不能为空")
    private String name;
    
    private String description;
    
    private String embeddingModel;
}

