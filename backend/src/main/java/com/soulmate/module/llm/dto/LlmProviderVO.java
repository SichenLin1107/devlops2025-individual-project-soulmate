package com.soulmate.module.llm.dto;

import com.soulmate.module.llm.entity.LlmProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * LLM提供商VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmProviderVO {
    
    private String id;
    private String name;
    private String apiBase;
    private String apiKey;
    private Integer isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 从实体转换为VO
     */
    public static LlmProviderVO fromEntity(LlmProvider entity) {
        if (entity == null) {
            return null;
        }
        return LlmProviderVO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .apiBase(entity.getApiBase())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    /**
     * 从实体转换为VO（包含解密后的apiKey）
     */
    public static LlmProviderVO fromEntity(LlmProvider entity, String decryptedApiKey) {
        LlmProviderVO vo = fromEntity(entity);
        if (vo != null) {
            vo.setApiKey(decryptedApiKey);
        }
        return vo;
    }
}
