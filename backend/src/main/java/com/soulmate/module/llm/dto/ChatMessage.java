package com.soulmate.module.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LLM消息格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    
    private String role;
    private String content;
}

