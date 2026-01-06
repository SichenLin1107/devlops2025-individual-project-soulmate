package com.soulmate.module.agent.controller;

import com.soulmate.common.ApiResponse;
import com.soulmate.common.PageResult;
import com.soulmate.module.agent.dto.*;
import com.soulmate.module.agent.service.AgentService;
import com.soulmate.module.knowledge.dto.KnowledgeBaseVO;
import com.soulmate.security.RequireRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 智能体控制器
 */
@RestController
@RequestMapping("/api/v1/agents")
@RequiredArgsConstructor
public class AgentController {
    
    private final AgentService agentService;
    
    /**
     * 获取智能体列表
     */
    @GetMapping
    public ApiResponse<PageResult<AgentVO>> listAgents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort) {
        PageResult<AgentVO> result = agentService.listAgents(page, size, status, tag, keyword, sort);
        return ApiResponse.success(result);
    }
    
    /**
     * 获取智能体详情
     */
    @GetMapping("/{id}")
    public ApiResponse<AgentVO> getAgent(@PathVariable String id) {
        AgentVO agent = agentService.getAgent(id);
        return ApiResponse.success(agent);
    }
    
    /**
     * 创建智能体
     */
    @PostMapping
    @RequireRole("admin")
    public ApiResponse<Map<String, String>> createAgent(@Valid @RequestBody AgentCreateRequest request) {
        String id = agentService.createAgent(request);
        return ApiResponse.success(Map.of("id", id));
    }
    
    /**
     * 更新智能体
     */
    @PutMapping("/{id}")
    @RequireRole("admin")
    public ApiResponse<Void> updateAgent(@PathVariable String id, @RequestBody AgentUpdateRequest request) {
        agentService.updateAgent(id, request);
        return ApiResponse.success();
    }
    
    /**
     * 删除智能体
     */
    @DeleteMapping("/{id}")
    @RequireRole("admin")
    public ApiResponse<Void> deleteAgent(@PathVariable String id) {
        agentService.deleteAgent(id);
        return ApiResponse.success();
    }
    
    /**
     * 更新智能体状态
     */
    @PutMapping("/{id}/status")
    @RequireRole("admin")
    public ApiResponse<Void> updateAgentStatus(@PathVariable String id, @Valid @RequestBody AgentStatusRequest request) {
        agentService.updateAgentStatus(id, request.getStatus());
        return ApiResponse.success();
    }
    
    /**
     * 绑定知识库
     */
    @PostMapping("/{id}/knowledge")
    @RequireRole("admin")
    public ApiResponse<Void> bindKnowledgeBases(@PathVariable String id, @Valid @RequestBody AgentKnowledgeRequest request) {
        agentService.bindKnowledgeBases(id, request.getKbIds());
        return ApiResponse.success();
    }
    
    /**
     * 解绑知识库
     */
    @DeleteMapping("/{id}/knowledge/{kbId}")
    @RequireRole("admin")
    public ApiResponse<Void> unbindKnowledgeBase(@PathVariable String id, @PathVariable String kbId) {
        agentService.unbindKnowledgeBase(id, kbId);
        return ApiResponse.success();
    }
    
    /**
     * 获取智能体绑定的知识库列表
     */
    @GetMapping("/{id}/knowledge")
    @RequireRole("admin")
    public ApiResponse<List<KnowledgeBaseVO>> getAgentKnowledgeBases(@PathVariable String id) {
        List<KnowledgeBaseVO> kbs = agentService.getAgentKnowledgeBases(id);
        return ApiResponse.success(kbs);
    }
}
