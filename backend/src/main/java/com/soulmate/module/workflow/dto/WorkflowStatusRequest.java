package com.soulmate.module.workflow.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 工作流状态更新请求
 */
@Data
public class WorkflowStatusRequest {
    
    @NotNull(message = "状态不能为空")
    private Integer isActive;
    
    /**
     * 是否同时禁用关联的智能体（仅在禁用时有效）
     */
    private Boolean disableRelatedAgents;
}
