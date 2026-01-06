package com.soulmate.module.sensitive.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 敏感词实体类
 */
@Data
public class SysSensitiveWord {
    
    private Long id;
    private String word;
    private String category;
    private String action;
    private String replacement;
    private Integer isActive;
    private LocalDateTime createdAt;
}

