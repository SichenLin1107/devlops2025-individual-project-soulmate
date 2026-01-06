package com.soulmate.module.llm.dto;

import com.soulmate.module.llm.entity.LlmModel;
import com.soulmate.util.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * LLM模型VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmModelVO {
    
    private String id;
    private String providerId;
    private String name;
    private String displayName;
    private String modelType;
    private String apiBase;
    private String apiKey;
    private Map<String, Object> defaultConfig;
    private Integer isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 从实体转换为VO
     */
    public static LlmModelVO fromEntity(LlmModel entity) {
        if (entity == null) {
            return null;
        }
        return LlmModelVO.builder()
                .id(entity.getId())
                .providerId(entity.getProviderId())
                .name(entity.getName())
                .displayName(entity.getDisplayName())
                .modelType(entity.getModelType())
                .apiBase(entity.getApiBase())
                .defaultConfig(JsonUtil.parseMap(entity.getDefaultConfig()))
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    /**
     * 从实体转换为VO（包含解密后的apiKey）
     */
    public static LlmModelVO fromEntity(LlmModel entity, String decryptedApiKey) {
        LlmModelVO vo = fromEntity(entity);
        if (vo != null) {
            vo.setApiKey(decryptedApiKey);
        }
        return vo;
    }
}
