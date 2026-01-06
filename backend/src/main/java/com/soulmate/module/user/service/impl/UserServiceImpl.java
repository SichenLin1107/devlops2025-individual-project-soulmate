package com.soulmate.module.user.service.impl;

import com.soulmate.common.BusinessException;
import com.soulmate.common.ErrorCode;
import com.soulmate.common.PageResult;
import com.soulmate.module.user.dto.UserVO;
import com.soulmate.module.user.dto.ChangePasswordRequest;
import com.soulmate.module.user.dto.UserUpdateRequest;
import com.soulmate.module.user.entity.SysUser;
import com.soulmate.module.user.mapper.SysUserMapper;
import com.soulmate.module.user.service.UserService;
import com.soulmate.security.UserContext;
import com.soulmate.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${file.upload.avatar-path:uploads/avatars}")
    private String avatarUploadPath;
    
    /**
     * 验证用户是否存在，不存在则抛出异常
     */
    private SysUser validateUserExists(String userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }
    
    /**
     * 验证是否为超级管理员
     */
    private void validateSuperAdmin() {
        if (!UserContext.isSuperAdmin()) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "只有超级管理员可以执行此操作");
        }
    }
    
    @Override
    public PageResult<UserVO> listUsers(int page, int size, String role, Integer status, String keyword) {
        validateSuperAdmin();
        
        int offset = (page - 1) * size;
        List<SysUser> users = userMapper.selectPage(offset, size, role, status, keyword);
        long total = userMapper.countByCondition(role, status, keyword);
        
        List<UserVO> list = users.stream()
                .map(UserVO::fromEntity)
                .collect(Collectors.toList());
        
        return PageResult.of(list, total, page, size);
    }
    
    @Override
    public UserVO getUserById(String id) {
        SysUser user = validateUserExists(id);
        return UserVO.fromEntity(user);
    }
    
    @Override
    public void updateUser(String id, UserUpdateRequest request) {
        validateSuperAdmin();
        SysUser user = validateUserExists(id);
        
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getRole() != null) {
            String newRole = request.getRole();
            if (!"user".equals(newRole) && !"admin".equals(newRole)) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "角色只能设置为user或admin");
            }
            user.setRole(newRole);
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        
        userMapper.update(user);
        log.info("用户更新成功: userId={}, updatedBy={}", id, UserContext.getUserId());
    }
    
    @Override
    public void deleteUser(String id) {
        validateSuperAdmin();
        SysUser user = validateUserExists(id);
        
        if ("superadmin".equals(user.getRole())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "不能删除超级管理员用户");
        }
        
        userMapper.deleteById(id);
        log.info("用户删除成功: userId={}, deletedBy={}", id, UserContext.getUserId());
    }
    
    @Override
    public void updateUserStatus(String id, Integer status) {
        validateSuperAdmin();
        validateUserExists(id);
        
        userMapper.updateStatus(id, status);
        log.info("用户状态更新: userId={}, status={}, updatedBy={}", id, status, UserContext.getUserId());
    }
    
    @Override
    public UserVO getProfile() {
        String userId = UserContext.getUserId();
        SysUser user = validateUserExists(userId);
        return UserVO.fromEntity(user);
    }
    
    @Override
    public void updateProfile(UserUpdateRequest request) {
        String userId = UserContext.getUserId();
        SysUser user = validateUserExists(userId);
        
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        
        userMapper.update(user);
        log.info("个人信息更新成功: userId={}", userId);
    }
    
    @Override
    public void changePassword(ChangePasswordRequest request) {
        String userId = UserContext.getUserId();
        SysUser user = validateUserExists(userId);
        
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR, "旧密码错误");
        }
        
        String newPassword = passwordEncoder.encode(request.getNewPassword());
        userMapper.updatePassword(userId, newPassword);
        log.info("密码修改成功: userId={}", userId);
    }
    
    // 头像上传功能已禁用
    // @Override
    // public String uploadAvatar(MultipartFile file) {
    //     String userId = UserContext.getUserId();
    //     SysUser user = validateUserExists(userId);
    //     
    //     try {
    //         String absolutePath = new File(avatarUploadPath).getAbsolutePath();
    //         String avatarUrl = FileUtil.uploadAvatar(file, absolutePath);
    //         
    //         if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
    //             String oldAvatarPath = user.getAvatar();
    //             if (oldAvatarPath.startsWith("/uploads/avatars/")) {
    //                 String oldFileName = oldAvatarPath.substring("/uploads/avatars/".length());
    //                 String oldFilePath = new File(absolutePath, oldFileName).getAbsolutePath();
    //                 FileUtil.deleteFile(oldFilePath);
    //             }
    //         }
    //         
    //         user.setAvatar(avatarUrl);
    //         userMapper.update(user);
    //         
    //         log.info("头像上传成功: userId={}, avatarUrl={}", userId, avatarUrl);
    //         return avatarUrl;
    //         
    //     } catch (IllegalArgumentException e) {
    //         throw new BusinessException(ErrorCode.PARAM_ERROR, e.getMessage());
    //     } catch (Exception e) {
    //         log.error("头像上传失败: userId={}", userId, e);
    //         throw new BusinessException(ErrorCode.SYSTEM_ERROR, "头像上传失败: " + e.getMessage());
    //     }
    // }
}
