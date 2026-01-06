package com.soulmate.module.auth.service;

import com.soulmate.module.auth.dto.LoginRequest;
import com.soulmate.module.auth.dto.LoginResponse;
import com.soulmate.module.auth.dto.RegisterRequest;
import com.soulmate.module.auth.dto.UserVO;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 用户注册
     */
    LoginResponse register(RegisterRequest request);
    
    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);
    
    /**
     * 获取当前登录用户信息
     */
    UserVO getCurrentUser();
    
    /**
     * 用户登出
     */
    void logout();
}
