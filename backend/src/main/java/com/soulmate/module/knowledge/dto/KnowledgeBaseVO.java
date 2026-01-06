package com.soulmate.module.knowledge.dto;

import com.soulmate.module.knowledge.entity.AiKnowledgeBase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 知识库视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBaseVO {
    
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
    
    public static KnowledgeBaseVO fromEntity(AiKnowledgeBase entity) {
        if (entity == null) {
            return null;
        }
        return KnowledgeBaseVO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .embeddingModel(entity.getEmbeddingModel())
                .docCount(entity.getDocCount())
                .segmentCount(entity.getSegmentCount())
                .isActive(entity.getIsActive())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
