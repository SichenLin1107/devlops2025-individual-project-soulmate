package com.soulmate.module.chat.service;

import com.soulmate.common.PageResult;
import com.soulmate.module.chat.dto.*;

/**
 * 消息服务接口
 */
public interface MessageService {
    /**
     * 获取消息列表
     */
    PageResult<MessageVO> listMessages(Long sessionId, int page, int size);
    
    /**
     * 发送消息
     */
    SendMessageResponse sendMessage(Long sessionId, SendMessageRequest request);
}

