package com.soulmate.module.chat.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息实体类
 */
@Data
public class ChatMessage {
    private Long id;
    private Long sessionId;
    /** 角色：user-用户, assistant-助手, system-系统 */
    private String role;
    private String content;
    /** 消息类型：text-文本, greeting-开场白, crisis_alert-危机干预 */
    private String msgType;
    /** 识别的情绪标签 */
    private String emotion;
    private Integer tokenCount;
    private LocalDateTime createdAt;
}

