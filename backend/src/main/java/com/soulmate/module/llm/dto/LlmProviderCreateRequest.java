package com.soulmate.module.llm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * LLM提供商创建/更新请求
 */
@Data
public class LlmProviderCreateRequest {
    
    @NotBlank(message = "提供商名称不能为空")
    private String name;
    
    private String apiBase;
    private String apiKey;
    private Integer isActive = 1;
}
