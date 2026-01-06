package com.soulmate.module.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 发送消息响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageResponse {
    private MessageVO userMessage;
    private MessageVO assistantMessage;
    private String sessionTitle;
    /** 引用的知识库名称列表，用于前端展示参考提示 */
    private List<String> referencedKnowledgeBases;
}

