package com.soulmate.module.workflow.service;

import com.soulmate.common.PageResult;
import com.soulmate.module.workflow.dto.NodeDefinition;
import com.soulmate.module.workflow.dto.WorkflowRequest;
import com.soulmate.module.workflow.dto.WorkflowVO;

import java.util.List;
import java.util.Map;

/**
 * 工作流服务接口
 */
public interface WorkflowService {
    
    /**
     * 获取工作流列表（分页）
     */
    PageResult<WorkflowVO> listWorkflows(int page, int size, String keyword, Integer isActive);
    
    /**
     * 获取所有节点定义
     */
    List<NodeDefinition> getNodeDefinitions();
    
    /**
     * 验证工作流配置
     * @param nodesConfig 节点配置
     * @return 验证结果 {valid: boolean, errors: List, warnings: List}
     */
    Map<String, Object> validateWorkflow(Map<String, Object> nodesConfig);
    
    /**
     * 获取工作流详情
     */
    WorkflowVO getWorkflow(String id);
    
    /**
     * 创建工作流
     */
    String createWorkflow(WorkflowRequest request);
    
    /**
     * 更新工作流
     */
    void updateWorkflow(String id, WorkflowRequest request);
    
    /**
     * 删除工作流
     */
    void deleteWorkflow(String id);
    
    /**
     * 更新工作流状态
     * @param id 工作流ID
     * @param isActive 是否启用: 1-启用, 0-禁用
     * @param disableRelatedAgents 是否同时禁用关联的智能体
     */
    void updateWorkflowStatus(String id, Integer isActive, boolean disableRelatedAgents);
    
    /**
     * 统计关联的智能体数量
     * @param id 工作流ID
     * @return 关联的智能体数量
     */
    int countRelatedAgents(String id);
    
    /**
     * 执行工作流（调试模式）
     * @param id 工作流ID
     * @param data 执行数据，包含 userInput 等
     * @return 执行结果
     */
    Map<String, Object> executeWorkflow(String id, Map<String, Object> data);
}
