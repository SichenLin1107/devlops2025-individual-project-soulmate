package com.soulmate.module.workflow.dto;

import lombok.Data;

import java.util.Map;

/**
 * 工作流更新请求
 */
@Data
public class WorkflowUpdateRequest {
    
    private String name;
    private String description;
    private Map<String, Object> nodesConfig;
    private Integer isActive;
}

