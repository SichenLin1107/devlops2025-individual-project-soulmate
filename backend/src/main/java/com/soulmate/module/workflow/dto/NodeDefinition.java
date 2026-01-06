package com.soulmate.module.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 节点类型定义
 * 用于前端展示节点可用的输入/输出端口
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeDefinition {
    
    /**
     * 节点类型（如：start, sensitive_word_check等）
     */
    private String type;
    
    /**
     * 节点显示名称
     */
    private String name;
    
    /**
     * 节点图标（前端使用）
     */
    private String icon;
    
    /**
     * 节点颜色（前端使用）
     */
    private String color;
    
    /**
     * 节点分类（control, safety, llm, retrieval等）
     */
    private String category;
    
    /**
     * 输入端口定义
     * 每个输入端口对应一个入箭头
     */
    private List<PortDefinition> inputs;
    
    /**
     * 输出端口定义
     * 每个输出端口对应一个出箭头
     */
    private List<PortDefinition> outputs;
    
    /**
     * 配置参数Schema（用于前端表单生成）
     */
    private Map<String, Object> configSchema;
    
    /**
     * 端口定义
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortDefinition {
        /**
         * 端口ID（唯一标识）
         */
        private String id;
        
        /**
         * 端口显示名称
         */
        private String name;
        
        /**
         * 数据类型（string, number, boolean, object等）
         */
        private String dataType;
        
        /**
         * 是否必填
         */
        private Boolean required;
        
        /**
         * 描述
         */
        private String description;
    }
}

