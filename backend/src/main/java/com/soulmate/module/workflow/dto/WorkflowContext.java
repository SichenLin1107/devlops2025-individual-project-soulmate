package com.soulmate.module.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 工作流执行上下文
 * 所有节点共享的上下文对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowContext {
    
    /**
     * 原始输入
     */
    private String userInput;
    private Long sessionId;
    private String agentId;
    
    /**
     * 节点输出（按节点ID存储）
     */
    @Builder.Default
    private Map<String, NodeOutput> nodeOutputs = new HashMap<>();
    
    /**
     * 全局变量（跨节点共享）
     */
    @Builder.Default
    private Map<String, Object> variables = new HashMap<>();
    
    /**
     * 执行元数据
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    
    /**
     * 获取节点输出
     */
    public NodeOutput getNodeOutput(String nodeId) {
        return nodeOutputs.get(nodeId);
    }
    
    /**
     * 设置节点输出
     */
    public void setNodeOutput(String nodeId, NodeOutput output) {
        nodeOutputs.put(nodeId, output);
    }
    
    /**
     * 获取变量
     */
    public Object getVariable(String key) {
        return variables.get(key);
    }
    
    /**
     * 设置变量
     */
    public void setVariable(String key, Object value) {
        variables.put(key, value);
    }
    
    /**
     * 获取最终回复
     */
    public String getFinalResponse() {
        return (String) variables.get("final_response");
    }
    
    /**
     * 设置最终回复
     */
    public void setFinalResponse(String response) {
        variables.put("final_response", response);
    }
    
    /**
     * 获取情绪
     */
    public String getEmotion() {
        return (String) variables.get("emotion");
    }
    
    /**
     * 设置情绪
     */
    public void setEmotion(String emotion) {
        variables.put("emotion", emotion);
    }
    
    /**
     * 是否危机
     */
    public Boolean isCrisis() {
        Object value = variables.get("is_crisis");
        return value instanceof Boolean ? (Boolean) value : false;
    }
    
    /**
     * 设置危机状态
     */
    public void setCrisis(boolean isCrisis) {
        variables.put("is_crisis", isCrisis);
    }
}

