package com.soulmate.module.agent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新智能体状态请求
 */
@Data
public class AgentStatusRequest {
    
    @NotBlank(message = "状态不能为空")
    private String status;
}
