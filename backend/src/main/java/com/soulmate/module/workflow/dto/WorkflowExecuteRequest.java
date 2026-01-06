package com.soulmate.module.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 工作流执行请求
 */
@Data
public class WorkflowExecuteRequest {
    
    @NotBlank(message = "用户输入不能为空")
    private String userInput;
    
    private Long sessionId;
    private String agentId;
}

