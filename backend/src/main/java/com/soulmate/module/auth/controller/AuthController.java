package com.soulmate.module.auth.controller;

import com.soulmate.common.ApiResponse;
import com.soulmate.module.auth.dto.*;
import com.soulmate.module.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return ApiResponse.success(response);
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success(response);
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ApiResponse<UserVO> me() {
        UserVO user = authService.getCurrentUser();
        return ApiResponse.success(user);
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.success();
    }
}
