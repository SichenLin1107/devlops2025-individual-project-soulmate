package com.soulmate.module.sensitive.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 敏感词创建/更新请求
 */
@Data
public class SensitiveWordRequest {
    
    @NotBlank(message = "敏感词不能为空")
    private String word;
    
    @NotBlank(message = "分类不能为空")
    private String category;
    
    @NotBlank(message = "处理动作不能为空")
    private String action;
    
    private String replacement;
    
    private Integer isActive = 1;
}

