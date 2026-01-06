package com.soulmate.module.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户状态更新请求
 */
@Data
public class UserStatusRequest {
    
    @NotNull(message = "状态不能为空")
    private Integer status;
}
