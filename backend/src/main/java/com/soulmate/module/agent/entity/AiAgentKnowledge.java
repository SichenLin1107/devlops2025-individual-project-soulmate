package com.soulmate.module.agent.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 智能体-知识库关联实体类
 */
@Data
public class AiAgentKnowledge {
    
    private Long id;
    private String agentId;
    private String kbId;
    private LocalDateTime createdAt;
}

