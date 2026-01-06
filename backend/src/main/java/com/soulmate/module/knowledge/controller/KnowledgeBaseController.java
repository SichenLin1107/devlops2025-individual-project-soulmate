package com.soulmate.module.knowledge.controller;

import com.soulmate.common.ApiResponse;
import com.soulmate.common.PageResult;
import com.soulmate.module.knowledge.dto.*;
import com.soulmate.module.knowledge.service.KnowledgeBaseService;
import com.soulmate.security.RequireRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 知识库管理控制器
 */
@RestController
@RequestMapping("/api/v1/knowledge-bases")
@RequiredArgsConstructor
public class KnowledgeBaseController {
    
    private final KnowledgeBaseService knowledgeBaseService;
    
    /**
     * 获取知识库列表
     */
    @GetMapping
    @RequireRole("admin")
    public ApiResponse<PageResult<KnowledgeBaseVO>> listKnowledgeBases(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        PageResult<KnowledgeBaseVO> result = knowledgeBaseService.listKnowledgeBases(page, size, keyword);
        return ApiResponse.success(result);
    }
    
    /**
     * 获取知识库详情
     */
    @GetMapping("/{id}")
    @RequireRole("admin")
    public ApiResponse<KnowledgeBaseVO> getKnowledgeBase(@PathVariable String id) {
        KnowledgeBaseVO kb = knowledgeBaseService.getKnowledgeBase(id);
        return ApiResponse.success(kb);
    }
    
    /**
     * 创建知识库
     */
    @PostMapping
    @RequireRole("admin")
    public ApiResponse<Map<String, String>> createKnowledgeBase(@Valid @RequestBody KnowledgeBaseRequest request) {
        String id = knowledgeBaseService.createKnowledgeBase(request);
        return ApiResponse.success(Map.of("id", id));
    }
    
    /**
     * 更新知识库
     */
    @PutMapping("/{id}")
    @RequireRole("admin")
    public ApiResponse<Void> updateKnowledgeBase(@PathVariable String id, @Valid @RequestBody KnowledgeBaseRequest request) {
        knowledgeBaseService.updateKnowledgeBase(id, request);
        return ApiResponse.success();
    }
    
    /**
     * 删除知识库
     */
    @DeleteMapping("/{id}")
    @RequireRole("admin")
    public ApiResponse<Void> deleteKnowledgeBase(@PathVariable String id) {
        knowledgeBaseService.deleteKnowledgeBase(id);
        return ApiResponse.success();
    }
    
    /**
     * 获取文档列表
     */
    @GetMapping("/{id}/documents")
    @RequireRole("admin")
    public ApiResponse<PageResult<DocumentVO>> listDocuments(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        PageResult<DocumentVO> result = knowledgeBaseService.listDocuments(id, page, size, status);
        return ApiResponse.success(result);
    }
    
    /**
     * 删除文档
     */
    @DeleteMapping("/{id}/documents/{docId}")
    @RequireRole("admin")
    public ApiResponse<Void> deleteDocument(@PathVariable String id, @PathVariable Long docId) {
        knowledgeBaseService.deleteDocument(id, docId);
        return ApiResponse.success();
    }
    
    /**
     * 上传文档
     */
    @PostMapping("/{id}/documents")
    @RequireRole("admin")
    public ApiResponse<Map<String, Long>> uploadDocument(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {
        Long docId = knowledgeBaseService.uploadDocument(id, file);
        return ApiResponse.success(Map.of("docId", docId));
    }
    
    /**
     * 重试文档处理
     */
    @PostMapping("/{id}/documents/{docId}/retry")
    @RequireRole("admin")
    public ApiResponse<Void> retryDocument(@PathVariable String id, @PathVariable Long docId) {
        knowledgeBaseService.retryDocument(id, docId);
        return ApiResponse.success();
    }
    
    /**
     * 测试检索功能
     */
    @PostMapping("/{id}/test")
    @RequireRole("admin")
    public ApiResponse<List<RetrievalResult>> testRetrieval(
            @PathVariable String id,
            @Valid @RequestBody RetrievalTestRequest request) {
        List<RetrievalResult> results = knowledgeBaseService.testRetrieval(id, request);
        return ApiResponse.success(results);
    }
    
    /**
     * 获取文档状态
     */
    @GetMapping("/{id}/documents/{docId}/status")
    @RequireRole("admin")
    public ApiResponse<Map<String, Object>> getDocumentStatus(@PathVariable String id, @PathVariable Long docId) {
        Map<String, Object> status = knowledgeBaseService.getDocumentStatus(id, docId);
        return ApiResponse.success(status);
    }
    
    /**
     * 更新知识库状态
     */
    @PutMapping("/{id}/status")
    @RequireRole("admin")
    public ApiResponse<Void> updateKnowledgeBaseStatus(@PathVariable String id, @Valid @RequestBody KnowledgeBaseStatusRequest request) {
        knowledgeBaseService.updateKnowledgeBaseStatus(id, request.getIsActive(), 
                request.getDisableRelatedAgents() != null && request.getDisableRelatedAgents());
        return ApiResponse.success();
    }
    
    /**
     * 统计关联的智能体数量
     */
    @GetMapping("/{id}/related-agents-count")
    @RequireRole("admin")
    public ApiResponse<Map<String, Integer>> countRelatedAgents(@PathVariable String id) {
        int count = knowledgeBaseService.countRelatedAgents(id);
        return ApiResponse.success(Map.of("count", count));
    }
}
