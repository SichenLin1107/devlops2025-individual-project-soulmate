package com.soulmate.module.agent.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 更新智能体请求
 */
@Data
public class AgentUpdateRequest {
    
    private String name;
    private String avatar;
    private String description;
    private String tags;
    private String greeting;
    private String systemPrompt;
    private String modelId;
    private Map<String, Object> modelConfig;
    private String workflowId;
    private List<String> kbIds;
}

