package com.soulmate.module.user.controller;

import com.soulmate.common.ApiResponse;
import com.soulmate.common.PageResult;
import com.soulmate.module.user.dto.UserVO;
import com.soulmate.module.user.dto.UserStatusRequest;
import com.soulmate.module.user.dto.UserUpdateRequest;
import com.soulmate.module.user.service.UserService;
import com.soulmate.security.RequireRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping
    @RequireRole("superadmin")
    public ApiResponse<PageResult<UserVO>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        PageResult<UserVO> result = userService.listUsers(page, size, role, status, keyword);
        return ApiResponse.success(result);
    }
    
    @GetMapping("/{id}")
    @RequireRole("superadmin")
    public ApiResponse<UserVO> getUserById(@PathVariable String id) {
        UserVO user = userService.getUserById(id);
        return ApiResponse.success(user);
    }
    
    @PutMapping("/{id}")
    @RequireRole("superadmin")
    public ApiResponse<Void> updateUser(@PathVariable String id, @RequestBody UserUpdateRequest request) {
        userService.updateUser(id, request);
        return ApiResponse.success();
    }
    
    @DeleteMapping("/{id}")
    @RequireRole("superadmin")
    public ApiResponse<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ApiResponse.success();
    }
    
    @PutMapping("/{id}/status")
    @RequireRole("superadmin")
    public ApiResponse<Void> updateUserStatus(@PathVariable String id, @Valid @RequestBody UserStatusRequest request) {
        userService.updateUserStatus(id, request.getStatus());
        return ApiResponse.success();
    }
}
