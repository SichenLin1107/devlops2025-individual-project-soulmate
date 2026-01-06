package com.soulmate.module.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 工作流执行结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowResult {
    
    /**
     * 最终回复内容
     */
    private String response;
    
    /**
     * 识别的情绪
     */
    private String emotion;
    
    /**
     * 是否为危机干预
     */
    private Boolean isCrisis;
    
    /**
     * 执行元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 已执行的节点ID列表
     */
    private List<String> nodesExecuted;
    
    /**
     * Token使用情况
     */
    private TokenUsage tokenUsage;
    
    /**
     * Token使用情况
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenUsage {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
    }
}

