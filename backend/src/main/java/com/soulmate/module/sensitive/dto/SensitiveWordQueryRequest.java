package com.soulmate.module.sensitive.dto;

import lombok.Data;

/**
 * 敏感词查询请求DTO
 */
@Data
public class SensitiveWordQueryRequest {
    
    private Integer page = 1;
    private Integer size = 20;
    private String category;
    private Integer isActive;
    private String keyword;
}

