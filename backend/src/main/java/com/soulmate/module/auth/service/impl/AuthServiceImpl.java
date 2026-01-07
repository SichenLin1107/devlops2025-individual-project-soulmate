package com.soulmate.module.auth.service.impl;

import com.soulmate.common.BusinessException;
import com.soulmate.common.ErrorCode;
import com.soulmate.module.auth.dto.LoginRequest;
import com.soulmate.module.auth.dto.LoginResponse;
import com.soulmate.module.auth.dto.RegisterRequest;
import com.soulmate.module.auth.service.AuthService;
import com.soulmate.module.user.dto.UserVO;
import com.soulmate.module.user.entity.SysUser;
import com.soulmate.module.user.mapper.SysUserMapper;
import com.soulmate.security.JwtUtil;
import com.soulmate.security.UserContext;
import com.soulmate.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final SysUserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public LoginResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        SysUser existingUser = userMapper.selectByUsername(request.getUsername().trim());
        if (existingUser != null) {
            throw new BusinessException(ErrorCode.USER_EXISTS, "用户名已被使用，请更换");
        }
        
        // 创建用户
        SysUser user = new SysUser();
        user.setId(IdGenerator.userId());
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("user");
        user.setNickname(request.getNickname() != null && !request.getNickname().trim().isEmpty() 
                ? request.getNickname().trim() : request.getUsername().trim());
        user.setStatus(1);
        
        userMapper.insert(user);
        log.info("用户注册成功: userId={}, username={}", user.getId(), user.getUsername());
        
        // 生成Token并返回（注册成功后自动登录）
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        
        return LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .token(token)
                .expiresIn(jwtUtil.getExpirationSeconds())
                .build();
    }
    
    @Override
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        SysUser user = userMapper.selectByUsername(request.getUsername().trim());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("用户登录失败: 密码错误, username={}", request.getUsername());
            throw new BusinessException(ErrorCode.PASSWORD_ERROR, "密码错误，请重新输入");
        }
        
        // 检查账号状态
        if (user.getStatus() == 0) {
            log.warn("用户登录失败: 账号已被禁用, userId={}, username={}", user.getId(), user.getUsername());
            throw new BusinessException(ErrorCode.USER_DISABLED, "账号已被禁用，请联系管理员");
        }
        
        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        log.info("用户登录成功: userId={}, username={}, role={}", user.getId(), user.getUsername(), user.getRole());
        
        return LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .token(token)
                .expiresIn(jwtUtil.getExpirationSeconds())
                .build();
    }
    
    @Override
    public UserVO getCurrentUser() {
        SysUser user = UserContext.getCurrentUser();
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return UserVO.fromEntity(user);
    }
    
    @Override
    public void logout() {
        SysUser user = UserContext.getCurrentUser();
        if (user != null) {
            log.info("用户登出: userId={}, username={}", user.getId(), user.getUsername());
        }
    }
}
