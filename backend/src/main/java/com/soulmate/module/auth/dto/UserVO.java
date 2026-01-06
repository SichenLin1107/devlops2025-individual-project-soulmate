package com.soulmate.module.auth.dto;

import com.soulmate.module.user.entity.SysUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户信息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {
    
    private String id;
    private String username;
    private String role;
    private String nickname;
    private String avatar;
    private String bio;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 从实体转换为VO
     */
    public static UserVO fromEntity(SysUser user) {
        if (user == null) {
            return null;
        }
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

