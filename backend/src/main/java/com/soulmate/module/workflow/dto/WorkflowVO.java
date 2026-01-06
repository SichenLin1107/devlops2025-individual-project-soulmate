package com.soulmate.module.workflow.dto;

import com.soulmate.module.workflow.entity.AiWorkflow;
import com.soulmate.util.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 工作流VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowVO {
    
    private String id;
    private String name;
    private String description;
    private Map<String, Object> nodesConfig;
    
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
     * 节点类型列表
     */
    private List<String> nodeTypes;
    
    /**
     * 是否包含RAG节点
     */
    private Boolean hasRag;
    
    /**
     * 是否包含安全检测节点
     */
    private Boolean hasCrisisIntervention;
    
    /**
     * 是否启用
     */
    private Integer isActive;
    
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static WorkflowVO fromEntity(AiWorkflow entity) {
        if (entity == null) {
            return null;
        }
        return WorkflowVO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .nodesConfig(JsonUtil.parseMap(entity.getNodesConfig()))
                .status(entity.getStatus())
                .validationStatus(entity.getValidationStatus())
                .nodeCount(entity.getNodeCount())
                .nodeTypes(JsonUtil.parseList(entity.getNodeTypes(), String.class))
                .hasRag(entity.getHasRag() != null && entity.getHasRag() == 1)
                .hasCrisisIntervention(entity.getHasCrisisIntervention() != null && entity.getHasCrisisIntervention() == 1)
                .isActive(entity.getIsActive())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    /**
     * 列表VO（不包含nodesConfig详情）
     */
    public static WorkflowVO fromEntitySimple(AiWorkflow entity) {
        if (entity == null) {
            return null;
        }
        return WorkflowVO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .validationStatus(entity.getValidationStatus())
                .nodeCount(entity.getNodeCount())
                .nodeTypes(JsonUtil.parseList(entity.getNodeTypes(), String.class))
                .hasRag(entity.getHasRag() != null && entity.getHasRag() == 1)
                .hasCrisisIntervention(entity.getHasCrisisIntervention() != null && entity.getHasCrisisIntervention() == 1)
                .isActive(entity.getIsActive())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
