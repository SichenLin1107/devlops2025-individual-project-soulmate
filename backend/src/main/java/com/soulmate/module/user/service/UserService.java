package com.soulmate.module.user.service;

import com.soulmate.common.PageResult;
import com.soulmate.module.user.dto.UserVO;
import com.soulmate.module.user.dto.ChangePasswordRequest;
import com.soulmate.module.user.dto.UserUpdateRequest;

/**
 * 用户服务接口
 */
public interface UserService {
    
    PageResult<UserVO> listUsers(int page, int size, String role, Integer status, String keyword);
    UserVO getUserById(String id);
    void updateUser(String id, UserUpdateRequest request);
    void deleteUser(String id);
    void updateUserStatus(String id, Integer status);
    UserVO getProfile();
    void updateProfile(UserUpdateRequest request);
    void changePassword(ChangePasswordRequest request);
}
