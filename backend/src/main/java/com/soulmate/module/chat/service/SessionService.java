package com.soulmate.module.chat.service;

import com.soulmate.common.PageResult;
import com.soulmate.module.chat.dto.*;

/**
 * 会话服务接口
 */
public interface SessionService {
    /**
     * 获取会话列表
     */
    PageResult<SessionVO> listSessions(int page, int size, String agentId, String sessionType);
    
    /**
     * 获取会话详情
     */
    SessionVO getSession(Long id);
    
    /**
     * 创建会话
     */
    CreateSessionResponse createSession(CreateSessionRequest request);
    
    /**
     * 更新会话标题
     */
    void updateSessionTitle(Long id, String title);
    
    /**
     * 更新置顶状态
     */
    void updateSessionPinned(Long id, Integer isPinned);
    
    /**
     * 删除会话
     */
    void deleteSession(Long id);
}

