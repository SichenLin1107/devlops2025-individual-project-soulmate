package com.soulmate.module.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新会话标题请求
 */
@Data
public class UpdateTitleRequest {
    
    @NotBlank(message = "标题不能为空")
    @Size(min = 1, max = 50, message = "标题长度需在1-50字符之间")
    private String title;
}

