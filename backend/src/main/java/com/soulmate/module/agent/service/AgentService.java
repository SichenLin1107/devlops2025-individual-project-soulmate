package com.soulmate.module.agent.service;

import com.soulmate.common.PageResult;
import com.soulmate.module.agent.dto.*;
import com.soulmate.module.knowledge.dto.KnowledgeBaseVO;

import java.util.List;

/**
 * 智能体服务接口
 */
public interface AgentService {
    
    /**
     * 获取智能体列表
     */
    PageResult<AgentVO> listAgents(int page, int size, String status, String tag, String keyword, String sort);
    
    /**
     * 获取智能体详情
     */
    AgentVO getAgent(String id);
    
    /**
     * 创建智能体
     */
    String createAgent(AgentCreateRequest request);
    
    /**
     * 更新智能体
     */
    void updateAgent(String id, AgentUpdateRequest request);
    
    /**
     * 删除智能体
     */
    void deleteAgent(String id);
    
    /**
     * 更新智能体状态
     */
    void updateAgentStatus(String id, String status);
    
    /**
     * 绑定知识库
     */
    void bindKnowledgeBases(String agentId, List<String> kbIds);
    
    /**
     * 解绑知识库
     */
    void unbindKnowledgeBase(String agentId, String kbId);
    
    /**
     * 获取智能体绑定的知识库列表
     */
    List<KnowledgeBaseVO> getAgentKnowledgeBases(String agentId);
}
