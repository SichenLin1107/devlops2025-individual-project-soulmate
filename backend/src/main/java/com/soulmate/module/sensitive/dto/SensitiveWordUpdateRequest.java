package com.soulmate.module.sensitive.dto;

import lombok.Data;

/**
 * 敏感词更新请求
 */
@Data
public class SensitiveWordUpdateRequest {
    
    private String word;
    private String category;
    private String action;
    private String replacement;
    private Integer isActive;
}

