package com.soulmate.module.sensitive.controller;

import com.soulmate.common.ApiResponse;
import com.soulmate.common.PageResult;
import com.soulmate.module.sensitive.dto.SensitiveWordRequest;
import com.soulmate.module.sensitive.dto.SensitiveWordStatusRequest;
import com.soulmate.module.sensitive.dto.SensitiveWordVO;
import com.soulmate.module.sensitive.service.SensitiveWordService;
import com.soulmate.security.RequireRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 敏感词管理控制器
 */
@RestController
@RequestMapping("/api/v1/sensitive-words")
@RequiredArgsConstructor
public class SensitiveWordController {
    
    private final SensitiveWordService sensitiveWordService;
    
    /**
     * 获取敏感词列表
     */
    @GetMapping
    @RequireRole("admin")
    public ApiResponse<PageResult<SensitiveWordVO>> listSensitiveWords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Integer isActive,
            @RequestParam(required = false) String keyword) {
        PageResult<SensitiveWordVO> result = sensitiveWordService.listSensitiveWords(page, size, category, action, isActive, keyword);
        return ApiResponse.success(result);
    }
    
    /**
     * 创建敏感词
     */
    @PostMapping
    @RequireRole("admin")
    public ApiResponse<Map<String, Long>> createSensitiveWord(@Valid @RequestBody SensitiveWordRequest request) {
        Long id = sensitiveWordService.createSensitiveWord(request);
        return ApiResponse.success(Map.of("id", id));
    }
    
    /**
     * 更新敏感词
     */
    @PutMapping("/{id}")
    @RequireRole("admin")
    public ApiResponse<Void> updateSensitiveWord(@PathVariable Long id, @Valid @RequestBody SensitiveWordRequest request) {
        sensitiveWordService.updateSensitiveWord(id, request);
        return ApiResponse.success();
    }
    
    /**
     * 删除敏感词
     */
    @DeleteMapping("/{id}")
    @RequireRole("admin")
    public ApiResponse<Void> deleteSensitiveWord(@PathVariable Long id) {
        sensitiveWordService.deleteSensitiveWord(id);
        return ApiResponse.success();
    }
    
    /**
     * 更新敏感词状态
     */
    @PutMapping("/{id}/status")
    @RequireRole("admin")
    public ApiResponse<Void> updateSensitiveWordStatus(@PathVariable Long id, @Valid @RequestBody SensitiveWordStatusRequest request) {
        sensitiveWordService.updateSensitiveWordStatus(id, request.getIsActive());
        return ApiResponse.success();
    }
    
    /**
     * 批量更新敏感词状态
     */
    @PutMapping("/batch/status")
    @RequireRole("admin")
    public ApiResponse<Void> batchUpdateStatus(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) request.get("ids");
        Integer isActive = (Integer) request.get("isActive");
        sensitiveWordService.batchUpdateStatus(ids, isActive);
        return ApiResponse.success();
    }
    
    /**
     * 批量删除敏感词
     */
    @DeleteMapping("/batch")
    @RequireRole("admin")
    public ApiResponse<Void> batchDelete(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) request.get("ids");
        sensitiveWordService.batchDelete(ids);
        return ApiResponse.success();
    }
}
