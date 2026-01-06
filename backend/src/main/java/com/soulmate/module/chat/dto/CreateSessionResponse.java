package com.soulmate.module.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建会话响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionResponse {
    private Long id;
    private String agentId;
    private String title;
    /** 智能体开场白 */
    private String greeting;
}

