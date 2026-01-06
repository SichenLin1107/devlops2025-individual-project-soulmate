package com.soulmate.module.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 工作流节点配置
 * 对应前端工作流编辑器的完整配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodesConfig {
    
    /**
     * 节点列表
     */
    private List<Node> nodes;
    
    /**
     * 边（连线）列表
     */
    private List<Edge> edges;
    
    /**
     * 节点定义
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Node {
        /**
         * 节点ID（唯一标识，如：start, node_1等）
         */
        private String id;
        
        /**
         * 节点类型（start, sensitive_word_check等）
         */
        private String type;
        
        /**
         * 节点显示名称
         */
        private String name;
        
        /**
         * 节点配置参数（根据节点类型不同而不同）
         */
        private Map<String, Object> config;
        
        /**
         * 节点位置（前端编辑器用）
         */
        private Position position;
    }
    
    /**
     * 边（连线）定义
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Edge {
        /**
         * 源节点ID
         */
        private String source;
        
        /**
         * 源节点输出端口ID（可选，如果节点只有一个输出端口则可不填）
         */
        private String sourcePort;
        
        /**
         * 目标节点ID
         */
        private String target;
        
        /**
         * 目标节点输入端口ID（可选，如果节点只有一个输入端口则可不填）
         */
        private String targetPort;
    }
    
    /**
     * 节点位置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Position {
        private Double x;
        private Double y;
    }
}

