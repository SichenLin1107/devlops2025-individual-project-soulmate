package com.soulmate.module.chat.controller;

import com.soulmate.common.ApiResponse;
import com.soulmate.common.PageResult;
import com.soulmate.module.chat.dto.*;
import com.soulmate.module.chat.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 会话控制器
 */
@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionController {
    private final SessionService sessionService;
    
    /**
     * 获取会话列表
     */
    @GetMapping
    public ApiResponse<PageResult<SessionVO>> listSessions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String agentId,
            @RequestParam(required = false) String sessionType) {
        PageResult<SessionVO> result = sessionService.listSessions(page, size, agentId, sessionType);
        return ApiResponse.success(result);
    }
    
    /**
     * 创建会话
     */
    @PostMapping
    public ApiResponse<CreateSessionResponse> createSession(@Valid @RequestBody CreateSessionRequest request) {
        CreateSessionResponse response = sessionService.createSession(request);
        return ApiResponse.success(response);
    }
    
    /**
     * 获取会话详情
     */
    @GetMapping("/{id}")
    public ApiResponse<SessionVO> getSession(@PathVariable Long id) {
        SessionVO session = sessionService.getSession(id);
        return ApiResponse.success(session);
    }
    
    /**
     * 更新会话标题
     */
    @PutMapping("/{id}/title")
    public ApiResponse<Void> updateSessionTitle(@PathVariable Long id, @Valid @RequestBody UpdateTitleRequest request) {
        sessionService.updateSessionTitle(id, request.getTitle());
        return ApiResponse.success();
    }
    
    /**
     * 更新置顶状态
     */
    @PutMapping("/{id}/pin")
    public ApiResponse<Void> updateSessionPinned(@PathVariable Long id, @Valid @RequestBody UpdatePinRequest request) {
        sessionService.updateSessionPinned(id, request.getIsPinned());
        return ApiResponse.success();
    }
    
    /**
     * 删除会话
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSession(@PathVariable Long id) {
        sessionService.deleteSession(id);
        return ApiResponse.success();
    }
}

