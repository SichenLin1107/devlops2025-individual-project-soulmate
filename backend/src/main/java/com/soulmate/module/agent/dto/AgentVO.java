package com.soulmate.module.agent.dto;

import com.soulmate.module.agent.entity.AiAgent;
import com.soulmate.util.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 智能体视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentVO {
    
    private String id;
    private String name;
    private String avatar;
    private String description;
    private List<String> tags;
    private String greeting;
    private String systemPrompt;
    private String modelId;
    private Map<String, Object> modelConfig;
    private String workflowId;
    private String status;
    private Integer heatValue;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    
    /**
     * 解析标签字符串为列表
     */
    private static List<String> parseTags(String tags) {
        if (tags == null || tags.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(tags.split(","));
    }
    
    /**
     * 从实体转换为完整VO
     */
    public static AgentVO fromEntity(AiAgent entity) {
        if (entity == null) {
            return null;
        }
        return AgentVO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .avatar(entity.getAvatar())
                .description(entity.getDescription())
                .tags(parseTags(entity.getTags()))
                .greeting(entity.getGreeting())
                .systemPrompt(entity.getSystemPrompt())
                .modelId(entity.getModelId())
                .modelConfig(JsonUtil.parseMap(entity.getModelConfig()))
                .workflowId(entity.getWorkflowId())
                .status(entity.getStatus())
                .heatValue(entity.getHeatValue())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .publishedAt(entity.getPublishedAt())
                .build();
    }
    
    /**
     * 从实体转换为简化VO（用于列表展示）
     */
    public static AgentVO fromEntitySimple(AiAgent entity) {
        if (entity == null) {
            return null;
        }
        return AgentVO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .avatar(entity.getAvatar())
                .description(entity.getDescription())
                .tags(parseTags(entity.getTags()))
                .status(entity.getStatus())
                .heatValue(entity.getHeatValue())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
