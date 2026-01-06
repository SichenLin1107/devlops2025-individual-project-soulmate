package com.soulmate.module.chat.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新置顶状态请求
 */
@Data
public class UpdatePinRequest {
    @NotNull(message = "置顶状态不能为空")
    /** 置顶状态：1-置顶，0-取消置顶 */
    private Integer isPinned;
}

