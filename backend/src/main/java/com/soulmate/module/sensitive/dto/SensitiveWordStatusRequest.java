package com.soulmate.module.sensitive.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 敏感词状态更新请求
 */
@Data
public class SensitiveWordStatusRequest {
    
    @NotNull(message = "状态不能为空")
    private Integer isActive;
}
