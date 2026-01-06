package com.soulmate.module.chat.dto;

import com.soulmate.module.chat.entity.ChatSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionVO {
    private Long id;
    private String userId;
    private String agentId;
    private String agentName;
    private String agentAvatar;
    private String title;
    private String sessionType;
    private Integer isPinned;
    private Integer messageCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 从实体对象转换为VO
     */
    public static SessionVO fromEntity(ChatSession entity) {
        if (entity == null) {
            return null;
        }
        return SessionVO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .agentId(entity.getAgentId())
                .title(entity.getTitle())
                .sessionType(entity.getSessionType())
                .isPinned(entity.getIsPinned())
                .messageCount(entity.getMessageCount())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
