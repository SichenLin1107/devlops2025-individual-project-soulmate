package com.soulmate.module.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 头像上传响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvatarUploadResponse {
    
    private String avatarUrl;
}

