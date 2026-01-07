package com.soulmate.module.user.controller;

import com.soulmate.common.ApiResponse;
import com.soulmate.module.user.dto.ChangePasswordRequest;
import com.soulmate.module.user.dto.UserUpdateRequest;
import com.soulmate.module.user.dto.UserVO;
import com.soulmate.module.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 个人中心控制器
 */
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {
    
    private final UserService userService;
    
    @GetMapping
    public ApiResponse<UserVO> getProfile() {
        UserVO user = userService.getProfile();
        return ApiResponse.success(user);
    }
    
    @PutMapping
    public ApiResponse<Void> updateProfile(@RequestBody UserUpdateRequest request) {
        userService.updateProfile(request);
        return ApiResponse.success();
    }
    
    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ApiResponse.success();
    }
}
