package com.soulmate.module.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建会话请求
 */
@Data
public class CreateSessionRequest {
    @NotBlank(message = "智能体ID不能为空")
    private String agentId;
    
    private String title = "新对话";
    
    /** 会话类型：normal | debug */
    private String sessionType = "normal";
}

