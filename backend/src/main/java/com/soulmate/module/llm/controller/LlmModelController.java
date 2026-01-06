package com.soulmate.module.llm.controller;

import com.soulmate.common.ApiResponse;
import com.soulmate.module.llm.dto.LlmModelRequest;
import com.soulmate.module.llm.dto.LlmModelVO;
import com.soulmate.module.llm.dto.TestChatRequest;
import com.soulmate.module.llm.service.LlmModelService;
import com.soulmate.security.RequireRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * LLM模型管理控制器
 */
@RestController
@RequestMapping("/api/v1/llm/models")
@RequiredArgsConstructor
public class LlmModelController {
    
    private final LlmModelService modelService;
    
    /**
     * 获取模型列表
     */
    @GetMapping
    @RequireRole("admin")
    public ApiResponse<List<LlmModelVO>> listModels(
            @RequestParam(required = false) String providerId,
            @RequestParam(required = false) String modelType,
            @RequestParam(required = false) Integer isActive) {
        List<LlmModelVO> models = modelService.listModels(providerId, modelType, isActive);
        return ApiResponse.success(models);
    }
    
    /**
     * 创建模型
     */
    @PostMapping
    @RequireRole("admin")
    public ApiResponse<Map<String, String>> createModel(@Valid @RequestBody LlmModelRequest request) {
        String id = modelService.createModel(request);
        return ApiResponse.success(Map.of("id", id));
    }
    
    /**
     * 更新模型
     */
    @PutMapping("/{id}")
    @RequireRole("admin")
    public ApiResponse<Void> updateModel(@PathVariable String id, @Valid @RequestBody LlmModelRequest request) {
        modelService.updateModel(id, request);
        return ApiResponse.success();
    }
    
    /**
     * 删除模型
     */
    @DeleteMapping("/{id}")
    @RequireRole("admin")
    public ApiResponse<Void> deleteModel(@PathVariable String id) {
        modelService.deleteModel(id);
        return ApiResponse.success();
    }
    
    /**
     * 测试对话
     */
    @PostMapping("/test-chat")
    @RequireRole("admin")
    public ApiResponse<Map<String, String>> testChat(@Valid @RequestBody TestChatRequest request) {
        String response = modelService.testChat(request);
        return ApiResponse.success(Map.of("response", response));
    }
    
    /**
     * 查询关联的智能体数量
     */
    @GetMapping("/{id}/related-agents-count")
    @RequireRole("admin")
    public ApiResponse<Map<String, Integer>> countRelatedAgents(@PathVariable String id) {
        int count = modelService.countRelatedAgents(id);
        return ApiResponse.success(Map.of("count", count));
    }
}
