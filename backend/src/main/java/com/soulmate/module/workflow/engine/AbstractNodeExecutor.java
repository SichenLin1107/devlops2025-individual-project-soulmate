package com.soulmate.module.workflow.engine;

import com.soulmate.module.workflow.dto.NodeOutput;
import com.soulmate.module.workflow.dto.NodesConfig;
import com.soulmate.module.workflow.dto.WorkflowContext;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 节点执行器抽象基类
 * 提供通用的执行框架和工具方法
 */
@Slf4j
public abstract class AbstractNodeExecutor implements NodeExecutor {
    
    @Override
    public NodeOutput execute(NodesConfig.Node node, WorkflowContext context) {
        long startTime = System.currentTimeMillis();
        String timestamp = Instant.now().toString();
        
        try {
            log.debug("开始执行节点: id={}, type={}", node.getId(), node.getType());
            
            NodeOutput output = doExecute(node, context);
            
            // 补充通用字段
            output.setNodeId(node.getId());
            output.setNodeType(node.getType());
            output.setDurationMs(System.currentTimeMillis() - startTime);
            output.setTimestamp(timestamp);
            
            if (output.getStatus() == null) {
                output.setStatus("success");
            }
            
            log.debug("节点执行完成: id={}, status={}, duration={}ms", 
                    node.getId(), output.getStatus(), output.getDurationMs());
            
            return output;
            
        } catch (Exception e) {
            log.error("节点执行失败: id={}, type={}, error={}", node.getId(), node.getType(), e.getMessage(), e);
            
            return NodeOutput.builder()
                    .nodeId(node.getId())
                    .nodeType(node.getType())
                    .status("failed")
                    .error(e.getMessage())
                    .durationMs(System.currentTimeMillis() - startTime)
                    .timestamp(timestamp)
                    .data(new HashMap<>())
                    .build();
        }
    }
    
    /**
     * 子类实现具体的执行逻辑
     */
    protected abstract NodeOutput doExecute(NodesConfig.Node node, WorkflowContext context);
    
    /**
     * 获取节点配置参数
     * 同时支持驼峰命名(modelId)和下划线命名(model_id)
     */
    @SuppressWarnings("unchecked")
    protected <T> T getConfig(NodesConfig.Node node, String key, T defaultValue) {
        if (node.getConfig() == null) {
            return defaultValue;
        }
        
        // 先尝试原始key
        Object value = node.getConfig().get(key);
        
        // 如果没找到，尝试另一种命名格式
        if (value == null) {
            String alternateKey = convertNamingStyle(key);
            value = node.getConfig().get(alternateKey);
        }
        
        if (value == null) {
            return defaultValue;
        }
        try {
            return (T) value;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }
    
    /**
     * 转换命名风格：驼峰 <-> 下划线
     */
    private String convertNamingStyle(String key) {
        if (key == null || key.isEmpty()) {
            return key;
        }
        
        // 检查是否是下划线命名
        if (key.contains("_")) {
            // 转换为驼峰命名
            StringBuilder result = new StringBuilder();
            boolean capitalizeNext = false;
            for (char c : key.toCharArray()) {
                if (c == '_') {
                    capitalizeNext = true;
                } else if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    result.append(c);
                }
            }
            return result.toString();
        } else {
            // 转换为下划线命名
            StringBuilder result = new StringBuilder();
            for (char c : key.toCharArray()) {
                if (Character.isUpperCase(c)) {
                    result.append('_').append(Character.toLowerCase(c));
                } else {
                    result.append(c);
                }
            }
            return result.toString();
        }
    }
    
    /**
     * 创建成功输出
     */
    protected NodeOutput successOutput(Map<String, Object> data) {
        return NodeOutput.builder()
                .status("success")
                .data(data != null ? data : new HashMap<>())
                .build();
    }
    
    /**
     * 创建失败输出
     */
    protected NodeOutput failedOutput(String error) {
        return NodeOutput.builder()
                .status("failed")
                .error(error)
                .data(new HashMap<>())
                .build();
    }
    
    /**
     * 创建跳过输出
     */
    protected NodeOutput skippedOutput(String reason) {
        Map<String, Object> data = new HashMap<>();
        data.put("skip_reason", reason);
        return NodeOutput.builder()
                .status("skipped")
                .data(data)
                .build();
    }
}

