package com.soulmate.module.agent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 创建智能体请求
 */
@Data
public class AgentCreateRequest {
    
    @NotBlank(message = "智能体名称不能为空")
    private String name;
    
    private String avatar;
    
    private String description;
    
    private String tags;
    
    @NotBlank(message = "开场白不能为空")
    private String greeting;
    
    @NotBlank(message = "系统提示词不能为空")
    private String systemPrompt;
    
    @NotBlank(message = "模型ID不能为空")
    private String modelId;
    
    private Map<String, Object> modelConfig;
    
    private String workflowId;
    
    private List<String> kbIds;
}
