package com.soulmate.module.chat.controller;

import com.soulmate.common.ApiResponse;
import com.soulmate.common.PageResult;
import com.soulmate.module.chat.dto.*;
import com.soulmate.module.chat.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 消息控制器
 */
@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;
    
    /**
     * 获取消息列表
     */
    @GetMapping
    public ApiResponse<PageResult<MessageVO>> listMessages(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        PageResult<MessageVO> result = messageService.listMessages(sessionId, page, size);
        return ApiResponse.success(result);
    }
    
    /**
     * 发送消息
     */
    @PostMapping
    public ApiResponse<SendMessageResponse> sendMessage(
            @PathVariable Long sessionId,
            @Valid @RequestBody SendMessageRequest request) {
        SendMessageResponse response = messageService.sendMessage(sessionId, request);
        return ApiResponse.success(response);
    }
}

