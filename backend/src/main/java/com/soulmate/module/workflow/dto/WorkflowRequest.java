package com.soulmate.module.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 工作流创建/更新请求
 */
@Data
public class WorkflowRequest {
    
    @NotBlank(message = "工作流名称不能为空")
    private String name;
    
    private String description;
    
    @NotNull(message = "节点配置不能为空")
    private Map<String, Object> nodesConfig;
    
    private Integer isActive = 1;
}

