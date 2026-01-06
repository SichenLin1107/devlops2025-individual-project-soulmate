package com.soulmate.module.chat.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话实体类
 */
@Data
public class ChatSession {
    private Long id;
    private String userId;
    private String agentId;
    private String title;
    /** 会话类型：normal-普通, debug-调试 */
    private String sessionType;
    /** 是否置顶：1-是, 0-否 */
    private Integer isPinned;
    private Integer messageCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

