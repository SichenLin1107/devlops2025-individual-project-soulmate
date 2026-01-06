package com.soulmate.module.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 节点输出
 * 每个节点执行后产生的标准输出结构
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeOutput {
    
    /**
     * 节点ID
     */
    private String nodeId;
    
    /**
     * 节点类型
     */
    private String nodeType;
    
    /**
     * 执行状态：success, failed, skipped
     */
    private String status;
    
    /**
     * 节点特定输出数据（根据节点类型不同而不同）
     */
    private Map<String, Object> data;
    
    /**
     * 错误信息（status=failed时）
     */
    private String error;
    
    /**
     * 执行耗时（毫秒）
     */
    private Long durationMs;
    
    /**
     * 执行时间（ISO 8601格式）
     */
    private String timestamp;
}

