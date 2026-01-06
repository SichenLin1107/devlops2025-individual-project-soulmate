package com.soulmate.security;

import com.soulmate.common.BusinessException;
import com.soulmate.common.ErrorCode;
import com.soulmate.module.user.entity.SysUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 用户上下文工具类，提供获取当前登录用户信息的方法
 */
public final class UserContext {
    
    private UserContext() {}

    /**
     * 获取当前登录用户ID
     */
    public static String getUserId() {
        SysUser user = getCurrentUser();
        if (user == null || user.getId() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录或登录状态失效");
        }
        return user.getId();
    }

    /**
     * 获取当前登录用户角色
     */
    public static String getRole() {
        SysUser user = getCurrentUser();
        return user != null ? user.getRole() : null;
    }

    /**
     * 判断当前用户是否为管理员（包括admin和superadmin）
     */
    public static boolean isAdmin() {
        String role = getRole();
        return "admin".equals(role) || "superadmin".equals(role);
    }
    
    /**
     * 判断当前用户是否为超级管理员
     */
    public static boolean isSuperAdmin() {
        return "superadmin".equals(getRole());
    }

    /**
     * 获取当前登录用户对象
     */
    public static SysUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof SysUser) {
            return (SysUser) auth.getPrincipal();
        }
        return null;
    }

    /**
     * 判断用户是否已登录
     */
    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof SysUser;
    }
}

