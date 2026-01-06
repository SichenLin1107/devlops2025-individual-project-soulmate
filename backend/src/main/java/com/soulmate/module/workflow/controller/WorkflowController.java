package com.soulmate.module.workflow.controller;

import com.soulmate.common.ApiResponse;
import com.soulmate.common.PageResult;
import com.soulmate.module.workflow.dto.NodeDefinition;
import com.soulmate.module.workflow.dto.WorkflowRequest;
import com.soulmate.module.workflow.dto.WorkflowStatusRequest;
import com.soulmate.module.workflow.dto.WorkflowVO;
import com.soulmate.module.workflow.service.WorkflowService;
import com.soulmate.security.RequireRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 工作流管理控制器（Admin）
 */
@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowController {
    
    private final WorkflowService workflowService;
    
    /**
     * 工作流列表（分页）
     * GET /api/v1/workflows
     */
    @GetMapping
    @RequireRole("admin")
    public ApiResponse<PageResult<WorkflowVO>> listWorkflows(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer isActive) {
        PageResult<WorkflowVO> result = workflowService.listWorkflows(page, size, keyword, isActive);
        return ApiResponse.success(result);
    }
    
    /**
     * 工作流详情
     * GET /api/v1/workflows/{id}
     */
    @GetMapping("/{id}")
    @RequireRole("admin")
    public ApiResponse<WorkflowVO> getWorkflow(@PathVariable String id) {
        WorkflowVO workflow = workflowService.getWorkflow(id);
        return ApiResponse.success(workflow);
    }
    
    /**
     * 创建工作流
     * POST /api/v1/workflows
     */
    @PostMapping
    @RequireRole("admin")
    public ApiResponse<Map<String, String>> createWorkflow(@Valid @RequestBody WorkflowRequest request) {
        String id = workflowService.createWorkflow(request);
        return ApiResponse.success(Map.of("id", id));
    }
    
    /**
     * 更新工作流
     * PUT /api/v1/workflows/{id}
     */
    @PutMapping("/{id}")
    @RequireRole("admin")
    public ApiResponse<Void> updateWorkflow(@PathVariable String id, @Valid @RequestBody WorkflowRequest request) {
        workflowService.updateWorkflow(id, request);
        return ApiResponse.success();
    }
    
    /**
     * 删除工作流
     * DELETE /api/v1/workflows/{id}
     */
    @DeleteMapping("/{id}")
    @RequireRole("admin")
    public ApiResponse<Void> deleteWorkflow(@PathVariable String id) {
        workflowService.deleteWorkflow(id);
        return ApiResponse.success();
    }
    
    /**
     * 启用/禁用工作流
     * PUT /api/v1/workflows/{id}/status
     */
    @PutMapping("/{id}/status")
    @RequireRole("admin")
    public ApiResponse<Void> updateWorkflowStatus(@PathVariable String id, @Valid @RequestBody WorkflowStatusRequest request) {
        workflowService.updateWorkflowStatus(id, request.getIsActive(), request.getDisableRelatedAgents() != null && request.getDisableRelatedAgents());
        return ApiResponse.success();
    }
    
    /**
     * 查询关联的智能体数量
     * GET /api/v1/workflows/{id}/related-agents-count
     */
    @GetMapping("/{id}/related-agents-count")
    @RequireRole("admin")
    public ApiResponse<Map<String, Integer>> countRelatedAgents(@PathVariable String id) {
        int count = workflowService.countRelatedAgents(id);
        return ApiResponse.success(Map.of("count", count));
    }
    
    /**
     * 获取所有节点定义
     * GET /api/v1/workflows/node-definitions
     */
    @GetMapping("/node-definitions")
    @RequireRole("admin")
    public ApiResponse<List<NodeDefinition>> getNodeDefinitions() {
        List<NodeDefinition> definitions = workflowService.getNodeDefinitions();
        return ApiResponse.success(definitions);
    }
    
    /**
     * 验证工作流配置
     * POST /api/v1/workflows/validate
     */
    @PostMapping("/validate")
    @RequireRole("admin")
    public ApiResponse<Map<String, Object>> validateWorkflow(@RequestBody Map<String, Object> nodesConfig) {
        Map<String, Object> result = workflowService.validateWorkflow(nodesConfig);
        return ApiResponse.success(result);
    }
}
