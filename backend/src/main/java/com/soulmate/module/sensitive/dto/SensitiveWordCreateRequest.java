package com.soulmate.module.sensitive.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 敏感词创建请求
 */
@Data
public class SensitiveWordCreateRequest {
    
    @NotBlank(message = "敏感词不能为空")
    private String word;
    
    @NotBlank(message = "分类不能为空")
    private String category;  // general | crisis | prohibited
    
    @NotBlank(message = "处理动作不能为空")
    private String action;    // block | warn | replace | intervention
    
    private String replacement;  // action=replace时必填
    
    private Integer isActive = 1;
}
