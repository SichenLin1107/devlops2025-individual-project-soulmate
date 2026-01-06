package com.soulmate.module.user.dto;

import lombok.Data;

/**
 * 用户更新请求
 */
@Data
public class UserUpdateRequest {
    
    private String nickname;
    private String avatar;
    private String bio;
    private String role;
    private Integer status;
}
