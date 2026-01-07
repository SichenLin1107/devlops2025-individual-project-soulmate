package com.soulmate.security;

import com.soulmate.common.BusinessException;
import com.soulmate.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 权限注解处理器，处理@RequireRole注解的权限验证
 */
@Slf4j
@Aspect
@Component
public class RequireRoleAspect {

    /**
     * 环绕通知，处理权限验证
     */
    @Around("@annotation(com.soulmate.security.RequireRole) || @within(com.soulmate.security.RequireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireRole methodAnnotation = method.getAnnotation(RequireRole.class);
        
        RequireRole classAnnotation = null;
        if (methodAnnotation == null) {
            classAnnotation = method.getDeclaringClass().getAnnotation(RequireRole.class);
        }
        
        String requiredRole = methodAnnotation != null ? 
            methodAnnotation.value() : 
            (classAnnotation != null ? classAnnotation.value() : "user");
        
        if (!UserContext.isAuthenticated()) {
            log.warn("权限验证失败: 用户未登录, method={}", method.getName());
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
        }
        
        String currentRole = UserContext.getRole();
        if (!hasPermission(currentRole, requiredRole)) {
            String userId = UserContext.getUserId();
            log.warn("权限验证失败: userId={}, currentRole={}, requiredRole={}, method={}", 
                    userId, currentRole, requiredRole, method.getName());
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "权限不足，需要" + getRoleDisplayName(requiredRole) + "权限");
        }
        
        log.debug("权限验证通过: userId={}, role={}, method={}", 
                UserContext.getUserId(), currentRole, method.getName());
        
        return joinPoint.proceed();
    }
    
    /**
     * 检查用户是否有权限
     */
    private boolean hasPermission(String currentRole, String requiredRole) {
        if (currentRole == null) {
            return false;
        }
        
        if ("superadmin".equals(currentRole)) {
            return true;
        }
        
        if ("admin".equals(currentRole)) {
            return "admin".equals(requiredRole) || "user".equals(requiredRole);
        }
        
        if ("user".equals(currentRole)) {
            return "user".equals(requiredRole);
        }
        
        return false;
    }
    
    /**
     * 获取角色显示名称
     */
    private String getRoleDisplayName(String role) {
        return switch (role) {
            case "superadmin" -> "超级管理员";
            case "admin" -> "管理员";
            case "user" -> "用户";
            default -> role;
        };
    }
}
