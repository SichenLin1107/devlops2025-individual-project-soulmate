package com.soulmate.module.llm.service;

import com.soulmate.module.llm.dto.LlmProviderRequest;
import com.soulmate.module.llm.dto.LlmProviderVO;

import java.util.List;

/**
 * LLM提供商服务接口
 */
public interface LlmProviderService {
    
    /**
     * 获取提供商列表
     */
    List<LlmProviderVO> listProviders();
    
    /**
     * 创建提供商
     */
    String createProvider(LlmProviderRequest request);
    
    /**
     * 更新提供商
     */
    void updateProvider(String id, LlmProviderRequest request);
    
    /**
     * 删除提供商
     */
    void deleteProvider(String id);
}
