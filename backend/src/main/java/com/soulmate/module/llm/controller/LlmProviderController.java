package com.soulmate.module.llm.controller;

import com.soulmate.common.ApiResponse;
import com.soulmate.module.llm.dto.LlmProviderRequest;
import com.soulmate.module.llm.dto.LlmProviderVO;
import com.soulmate.module.llm.service.LlmProviderService;
import com.soulmate.security.RequireRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * LLM提供商管理控制器
 */
@RestController
@RequestMapping("/api/v1/llm/providers")
@RequiredArgsConstructor
public class LlmProviderController {
    
    private final LlmProviderService providerService;
    
    /**
     * 获取提供商列表
     */
    @GetMapping
    @RequireRole("admin")
    public ApiResponse<List<LlmProviderVO>> listProviders() {
        List<LlmProviderVO> providers = providerService.listProviders();
        return ApiResponse.success(providers);
    }
    
    /**
     * 创建提供商
     */
    @PostMapping
    @RequireRole("admin")
    public ApiResponse<Map<String, String>> createProvider(@Valid @RequestBody LlmProviderRequest request) {
        String id = providerService.createProvider(request);
        return ApiResponse.success(Map.of("id", id));
    }
    
    /**
     * 更新提供商
     */
    @PutMapping("/{id}")
    @RequireRole("admin")
    public ApiResponse<Void> updateProvider(@PathVariable String id, @Valid @RequestBody LlmProviderRequest request) {
        providerService.updateProvider(id, request);
        return ApiResponse.success();
    }
    
    /**
     * 删除提供商
     */
    @DeleteMapping("/{id}")
    @RequireRole("admin")
    public ApiResponse<Void> deleteProvider(@PathVariable String id) {
        providerService.deleteProvider(id);
        return ApiResponse.success();
    }
}
