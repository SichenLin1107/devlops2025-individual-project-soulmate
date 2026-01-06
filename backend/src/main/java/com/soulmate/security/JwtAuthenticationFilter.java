package com.soulmate.security;

import com.soulmate.module.user.entity.SysUser;
import com.soulmate.module.user.mapper.SysUserMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * JWT认证过滤器，处理Token验证和用户认证
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final SysUserMapper userMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if ("OPTIONS".equals(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            String token = getTokenFromRequest(request);
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                String userId = jwtUtil.getUserIdFromToken(token);
                
                if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    SysUser user = userMapper.selectById(userId);
                    
                    if (user != null && user.getStatus() == 1) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("JWT认证成功: userId={}, username={}", user.getId(), user.getUsername());
                    } else if (user != null) {
                        log.warn("JWT认证失败: 用户已被禁用, userId={}", userId);
                    } else {
                        log.warn("JWT认证失败: 用户不存在, userId={}", userId);
                    }
                }
            }
        } catch (JwtException e) {
            log.warn("JWT认证失败: Token解析异常, error={}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT认证异常: error={}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}