package com.soulmate.module.llm.service;

import com.soulmate.module.llm.dto.ChatMessage;

import java.util.List;
import java.util.Map;

/**
 * LLM统一调用客户端接口
 */
public interface LlmClient {
    
    /**
     * 调用LLM进行对话
     */
    String chat(String modelId, List<ChatMessage> messages, Map<String, Object> configOverride);
}

