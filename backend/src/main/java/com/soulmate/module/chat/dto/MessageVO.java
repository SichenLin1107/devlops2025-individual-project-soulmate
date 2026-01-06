package com.soulmate.module.chat.dto;

import com.soulmate.module.chat.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消息视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageVO {
    private Long id;
    private Long sessionId;
    private String role;
    private String content;
    private String msgType;
    private String emotion;
    private Integer tokenCount;
    private LocalDateTime createdAt;
    
    /**
     * 从实体对象转换为VO
     */
    public static MessageVO fromEntity(ChatMessage entity) {
        if (entity == null) {
            return null;
        }
        return MessageVO.builder()
                .id(entity.getId())
                .sessionId(entity.getSessionId())
                .role(entity.getRole())
                .content(entity.getContent())
                .msgType(entity.getMsgType())
                .emotion(entity.getEmotion())
                .tokenCount(entity.getTokenCount())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
