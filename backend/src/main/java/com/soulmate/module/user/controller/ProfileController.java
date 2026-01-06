package com.soulmate.module.user.controller;

import com.soulmate.common.ApiResponse;
import com.soulmate.module.user.dto.UserVO;
import com.soulmate.module.user.dto.AvatarUploadResponse;
import com.soulmate.module.user.dto.ChangePasswordRequest;
import com.soulmate.module.user.dto.UserUpdateRequest;
import com.soulmate.module.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    
    // 头像上传功能已禁用
    // @PostMapping("/avatar")
    // public ApiResponse<AvatarUploadResponse> uploadAvatar(@RequestParam("file") MultipartFile file) {
    //     String avatarUrl = userService.uploadAvatar(file);
    //     AvatarUploadResponse response = new AvatarUploadResponse(avatarUrl);
    //     return ApiResponse.success(response);
    // }
}
