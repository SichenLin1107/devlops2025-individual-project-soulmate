package com.soulmate.module.chat.service.impl;

import com.soulmate.common.BusinessException;
import com.soulmate.common.ErrorCode;
import com.soulmate.common.PageResult;
import com.soulmate.module.agent.entity.AiAgent;
import com.soulmate.module.agent.mapper.AiAgentMapper;
import com.soulmate.module.chat.dto.*;
import com.soulmate.module.chat.entity.ChatMessage;
import com.soulmate.module.chat.entity.ChatSession;
import com.soulmate.module.chat.mapper.ChatMessageMapper;
import com.soulmate.module.chat.mapper.ChatSessionMapper;
import com.soulmate.module.chat.service.SessionService;
import com.soulmate.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 会话服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {
    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final AiAgentMapper agentMapper;
    
    @Override
    public PageResult<SessionVO> listSessions(int page, int size, String agentId, String sessionType) {
        String userId = UserContext.getUserId();
        int offset = (page - 1) * size;
        
        List<ChatSession> sessions = sessionMapper.selectPage(userId, agentId, sessionType, offset, size);
        long total = sessionMapper.countByUserId(userId, agentId, sessionType);
        
        List<SessionVO> list = sessions.stream()
                .map(session -> {
                    SessionVO vo = SessionVO.fromEntity(session);
                    AiAgent agent = agentMapper.selectById(session.getAgentId());
                    if (agent != null) {
                        vo.setAgentName(agent.getName());
                        vo.setAgentAvatar(agent.getAvatar());
                    }
                    return vo;
                })
                .collect(Collectors.toList());
        
        return PageResult.of(list, total, page, size);
    }
    
    @Override
    public SessionVO getSession(Long id) {
        ChatSession session = validateAndGetSession(id);
        
        SessionVO vo = SessionVO.fromEntity(session);
        AiAgent agent = agentMapper.selectById(session.getAgentId());
        if (agent != null) {
            vo.setAgentName(agent.getName());
            vo.setAgentAvatar(agent.getAvatar());
        }
        
        return vo;
    }
    
    @Override
    @Transactional
    public CreateSessionResponse createSession(CreateSessionRequest request) {
        AiAgent agent = agentMapper.selectById(request.getAgentId());
        if (agent == null) {
            throw new BusinessException(ErrorCode.AGENT_NOT_FOUND);
        }
        if (!"published".equals(agent.getStatus())) {
            throw new BusinessException(ErrorCode.AGENT_OFFLINE);
        }
        
        ChatSession session = new ChatSession();
        session.setUserId(UserContext.getUserId());
        session.setAgentId(request.getAgentId());
        session.setTitle(request.getTitle() != null ? request.getTitle() : "新对话");
        session.setSessionType(request.getSessionType() != null ? request.getSessionType() : "normal");
        session.setIsPinned(0);
        session.setMessageCount(0);
        sessionMapper.insert(session);
        
        ChatMessage greetingMsg = new ChatMessage();
        greetingMsg.setSessionId(session.getId());
        greetingMsg.setRole("assistant");
        greetingMsg.setContent(agent.getGreeting());
        greetingMsg.setMsgType("greeting");
        messageMapper.insert(greetingMsg);
        
        sessionMapper.updateMessageCount(session.getId(), 1);
        
        log.info("会话创建成功: sessionId={}, userId={}, agentId={}", session.getId(), session.getUserId(), session.getAgentId());
        
        return CreateSessionResponse.builder()
                .id(session.getId())
                .agentId(request.getAgentId())
                .title(session.getTitle())
                .greeting(agent.getGreeting())
                .build();
    }
    
    @Override
    @Transactional
    public void updateSessionTitle(Long id, String title) {
        validateAndGetSession(id);
        sessionMapper.updateTitle(id, title);
        log.info("会话标题更新: sessionId={}, title={}", id, title);
    }
    
    @Override
    @Transactional
    public void updateSessionPinned(Long id, Integer isPinned) {
        validateAndGetSession(id);
        sessionMapper.updatePinned(id, isPinned);
        log.info("会话置顶更新: sessionId={}, isPinned={}", id, isPinned);
    }
    
    @Override
    @Transactional
    public void deleteSession(Long id) {
        validateAndGetSession(id);
        sessionMapper.deleteById(id);
        log.info("会话删除成功: sessionId={}", id);
    }
    
    /**
     * 验证会话存在性和访问权限
     */
    private ChatSession validateAndGetSession(Long id) {
        ChatSession session = sessionMapper.selectById(id);
        if (session == null) {
            throw new BusinessException(ErrorCode.SESSION_NOT_FOUND);
        }
        if (!session.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException(ErrorCode.SESSION_ACCESS_DENIED);
        }
        return session;
    }
}

