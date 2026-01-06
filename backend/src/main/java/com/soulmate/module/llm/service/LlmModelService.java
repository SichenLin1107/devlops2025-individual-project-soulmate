package com.soulmate.module.llm.service;

import com.soulmate.module.llm.dto.LlmModelRequest;
import com.soulmate.module.llm.dto.LlmModelVO;
import com.soulmate.module.llm.dto.TestChatRequest;

import java.util.List;

/**
 * LLM模型服务接口
 */
public interface LlmModelService {
    
    /**
     * 获取模型列表
     */
    List<LlmModelVO> listModels(String providerId, String modelType, Integer isActive);
    
    /**
     * 创建模型
     */
    String createModel(LlmModelRequest request);
    
    /**
     * 更新模型
     */
    void updateModel(String id, LlmModelRequest request);
    
    /**
     * 删除模型
     */
    void deleteModel(String id);
    
    /**
     * 测试对话
     */
    String testChat(TestChatRequest request);
    
    /**
     * 统计关联的智能体数量
     */
    int countRelatedAgents(String id);
}
