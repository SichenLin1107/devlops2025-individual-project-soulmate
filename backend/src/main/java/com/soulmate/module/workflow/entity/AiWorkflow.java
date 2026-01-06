package com.soulmate.module.workflow.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工作流实体类 - 对应 ai_workflow 表
 */
@Data
public class AiWorkflow {
    
    /**
     * 工作流ID (wfl_xxx)
     */
    private String id;
    
    /**
     * 工作流名称
     */
    private String name;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 节点与连线配置 (JSON格式)
     * 资源绑定在节点内
     */
    private String nodesConfig;
    
    /**
     * 工作流状态: draft-草稿, published-已发布, archived-已归档
     */
    private String status;
    
    /**
     * 验证状态: valid-通过, invalid-未通过, warning-有警告
     */
    private String validationStatus;
    
    /**
     * 节点数量
     */
    private Integer nodeCount;
    
    /**
     * 节点类型列表 (JSON格式，如 ["start", "llm_process", "end"])
     */
    private String nodeTypes;
    
    /**
     * 是否包含RAG节点: 1-是, 0-否
     */
    private Integer hasRag;
    
    /**
     * 是否包含安全检测节点（safety_check）: 1-是, 0-否
     */
    private Integer hasCrisisIntervention;
    
    /**
     * 是否启用: 1-是, 0-否（只有published状态才能启用）
     */
    private Integer isActive;
    
    /**
     * 创建者ID
     */
    private String createdBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

