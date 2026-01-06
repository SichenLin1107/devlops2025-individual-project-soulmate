package com.soulmate.module.user.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
public class SysUser {
    
    private String id;
    private String username;
    private String password;
    private String role;
    private String nickname;
    private String avatar;
    private String bio;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

