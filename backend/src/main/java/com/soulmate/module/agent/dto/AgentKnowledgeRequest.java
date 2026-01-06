package com.soulmate.module.agent.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 绑定知识库请求
 */
@Data
public class AgentKnowledgeRequest {
    
    @NotEmpty(message = "知识库ID列表不能为空")
    private List<String> kbIds;
}

