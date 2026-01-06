package com.soulmate.module.agent.service.impl;

import com.soulmate.common.BusinessException;
import com.soulmate.common.ErrorCode;
import com.soulmate.common.PageResult;
import com.soulmate.module.agent.dto.*;
import com.soulmate.module.agent.entity.AiAgent;
import com.soulmate.module.agent.entity.AiAgentKnowledge;
import com.soulmate.module.agent.mapper.AiAgentKnowledgeMapper;
import com.soulmate.module.agent.mapper.AiAgentMapper;
import com.soulmate.module.agent.service.AgentService;
import com.soulmate.module.knowledge.dto.KnowledgeBaseVO;
import com.soulmate.module.knowledge.entity.AiKnowledgeBase;
import com.soulmate.module.knowledge.mapper.AiKnowledgeBaseMapper;
import com.soulmate.security.UserContext;
import com.soulmate.util.IdGenerator;
import com.soulmate.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 智能体服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {
    
    private final AiAgentMapper agentMapper;
    private final AiAgentKnowledgeMapper agentKnowledgeMapper;
    private final AiKnowledgeBaseMapper knowledgeBaseMapper;
    
    @Override
    public PageResult<AgentVO> listAgents(int page, int size, String status, String tag, String keyword, String sort) {
        int offset = (page - 1) * size;
        List<AiAgent> agents;
        long total;
        
        if ("published".equals(status)) {
            agents = agentMapper.selectPublishedAgentsPage(offset, size, keyword, tag, sort);
            total = agentMapper.countPublishedAgents(keyword, tag);
        } else {
            agents = agentMapper.selectPage(offset, size, status, keyword, sort);
            total = agentMapper.countAll(status, keyword);
        }
        
        List<AgentVO> list = agents.stream()
                .map(AgentVO::fromEntitySimple)
                .collect(Collectors.toList());
        
        return PageResult.of(list, total, page, size);
    }
    
    @Override
    public AgentVO getAgent(String id) {
        AiAgent agent = requireAgentExists(id);
        return AgentVO.fromEntity(agent);
    }
    
    @Override
    @Transactional
    public String createAgent(AgentCreateRequest request) {
        AiAgent agent = new AiAgent();
        agent.setId(IdGenerator.agentId());
        agent.setName(request.getName());
        agent.setAvatar(request.getAvatar());
        agent.setDescription(request.getDescription());
        agent.setTags(request.getTags());
        agent.setGreeting(request.getGreeting());
        agent.setSystemPrompt(request.getSystemPrompt());
        agent.setModelId(request.getModelId());
        agent.setModelConfig(JsonUtil.toJson(request.getModelConfig()));
        agent.setWorkflowId(request.getWorkflowId());
        agent.setStatus("offline");
        agent.setHeatValue(0);
        agent.setCreatedBy(UserContext.getUserId());
        
        agentMapper.insert(agent);
        
        if (request.getKbIds() != null && !request.getKbIds().isEmpty()) {
            validateKnowledgeBases(request.getKbIds());
            bindKnowledgeBasesInternal(agent.getId(), request.getKbIds());
        }
        
        log.info("智能体创建成功: id={}, name={}", agent.getId(), agent.getName());
        return agent.getId();
    }
    
    @Override
    @Transactional
    public void updateAgent(String id, AgentUpdateRequest request) {
        requireAgentExists(id);
        
        AiAgent agent = new AiAgent();
        agent.setId(id);
        if (request.getName() != null) agent.setName(request.getName());
        if (request.getAvatar() != null) agent.setAvatar(request.getAvatar());
        if (request.getDescription() != null) agent.setDescription(request.getDescription());
        if (request.getTags() != null) agent.setTags(request.getTags());
        if (request.getGreeting() != null) agent.setGreeting(request.getGreeting());
        if (request.getSystemPrompt() != null) agent.setSystemPrompt(request.getSystemPrompt());
        if (request.getModelId() != null) agent.setModelId(request.getModelId());
        if (request.getModelConfig() != null) agent.setModelConfig(JsonUtil.toJson(request.getModelConfig()));
        if (request.getWorkflowId() != null) agent.setWorkflowId(request.getWorkflowId());
        
        agentMapper.update(agent);
        
        if (request.getKbIds() != null) {
            agentKnowledgeMapper.deleteByAgentId(id);
            if (!request.getKbIds().isEmpty()) {
                validateKnowledgeBases(request.getKbIds());
                bindKnowledgeBasesInternal(id, request.getKbIds());
            }
        }
        
        log.info("智能体更新成功: id={}", id);
    }
    
    @Override
    @Transactional
    public void deleteAgent(String id) {
        requireAgentExists(id);
        agentKnowledgeMapper.deleteByAgentId(id);
        agentMapper.deleteById(id);
        log.info("智能体删除成功: id={}", id);
    }
    
    @Override
    @Transactional
    public void updateAgentStatus(String id, String status) {
        requireAgentExists(id);
        
        if (!"published".equals(status) && !"offline".equals(status)) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "无效的状态值: " + status + "，有效值: published(启用), offline(下架)");
        }
        
        agentMapper.updateStatus(id, status);
        log.info("智能体状态更新: id={}, status={}", id, status);
    }
    
    @Override
    @Transactional
    public void bindKnowledgeBases(String agentId, List<String> kbIds) {
        requireAgentExists(agentId);
        validateKnowledgeBases(kbIds);
        
        List<AiAgentKnowledge> existingBindings = agentKnowledgeMapper.selectByAgentId(agentId);
        List<String> existingKbIds = existingBindings.stream()
                .map(AiAgentKnowledge::getKbId)
                .collect(Collectors.toList());
        
        for (String kbId : kbIds) {
            if (!existingKbIds.contains(kbId)) {
                AiAgentKnowledge ak = new AiAgentKnowledge();
                ak.setAgentId(agentId);
                ak.setKbId(kbId);
                agentKnowledgeMapper.insert(ak);
            }
        }
        
        log.info("智能体绑定知识库: agentId={}, kbIds={}", agentId, kbIds);
    }
    
    @Override
    @Transactional
    public void unbindKnowledgeBase(String agentId, String kbId) {
        requireAgentExists(agentId);
        requireKnowledgeBaseExists(kbId);
        
        int deleted = agentKnowledgeMapper.delete(agentId, kbId);
        if (deleted == 0) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "该知识库未绑定到此智能体");
        }
        
        log.info("智能体解绑知识库: agentId={}, kbId={}", agentId, kbId);
    }
    
    @Override
    public List<KnowledgeBaseVO> getAgentKnowledgeBases(String agentId) {
        requireAgentExists(agentId);
        
        List<AiAgentKnowledge> aks = agentKnowledgeMapper.selectByAgentId(agentId);
        return aks.stream()
                .map(ak -> {
                    AiKnowledgeBase kb = knowledgeBaseMapper.selectById(ak.getKbId());
                    return kb != null ? KnowledgeBaseVO.fromEntity(kb) : null;
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());
    }
    
    /**
     * 验证智能体是否存在
     */
    private AiAgent requireAgentExists(String id) {
        AiAgent agent = agentMapper.selectById(id);
        if (agent == null) {
            throw new BusinessException(ErrorCode.AGENT_NOT_FOUND);
        }
        return agent;
    }
    
    /**
     * 验证知识库是否存在
     */
    private void requireKnowledgeBaseExists(String kbId) {
        if (knowledgeBaseMapper.selectById(kbId) == null) {
            throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_NOT_FOUND);
        }
    }
    
    /**
     * 批量验证知识库是否存在
     */
    private void validateKnowledgeBases(List<String> kbIds) {
        for (String kbId : kbIds) {
            requireKnowledgeBaseExists(kbId);
        }
    }
    
    /**
     * 内部方法：绑定知识库（不进行重复检查）
     */
    private void bindKnowledgeBasesInternal(String agentId, List<String> kbIds) {
        for (String kbId : kbIds) {
            AiAgentKnowledge ak = new AiAgentKnowledge();
            ak.setAgentId(agentId);
            ak.setKbId(kbId);
            agentKnowledgeMapper.insert(ak);
        }
    }
}

