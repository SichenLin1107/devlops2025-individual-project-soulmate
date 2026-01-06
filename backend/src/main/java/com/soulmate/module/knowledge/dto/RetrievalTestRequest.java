package com.soulmate.module.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 检索测试请求
 */
@Data
public class RetrievalTestRequest {
    
    @NotBlank(message = "查询内容不能为空")
    private String query;
    
    private Integer topK = 3;
    private Double scoreThreshold = 0.5;
}

