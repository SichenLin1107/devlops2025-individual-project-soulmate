package com.soulmate.module.knowledge.service;

import com.soulmate.common.PageResult;
import com.soulmate.module.knowledge.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 知识库服务接口
 */
public interface KnowledgeBaseService {
    
    /**
     * 获取知识库列表
     */
    PageResult<KnowledgeBaseVO> listKnowledgeBases(int page, int size, String keyword);
    
    /**
     * 获取知识库详情
     */
    KnowledgeBaseVO getKnowledgeBase(String id);
    
    /**
     * 创建知识库
     */
    String createKnowledgeBase(KnowledgeBaseRequest request);
    
    /**
     * 更新知识库
     */
    void updateKnowledgeBase(String id, KnowledgeBaseRequest request);
    
    /**
     * 删除知识库
     */
    void deleteKnowledgeBase(String id);
    
    /**
     * 获取文档列表
     */
    PageResult<DocumentVO> listDocuments(String kbId, int page, int size, String status);
    
    /**
     * 删除文档
     */
    void deleteDocument(String kbId, Long docId);
    
    /**
     * 上传文档
     */
    Long uploadDocument(String kbId, MultipartFile file);
    
    /**
     * 重试文档处理
     */
    void retryDocument(String kbId, Long docId);
    
    /**
     * 测试检索功能
     */
    List<RetrievalResult> testRetrieval(String kbId, RetrievalTestRequest request);
    
    /**
     * 获取文档状态
     */
    Map<String, Object> getDocumentStatus(String kbId, Long docId);
    
    /**
     * 更新知识库状态
     */
    void updateKnowledgeBaseStatus(String id, Integer isActive, boolean disableRelatedAgents);
    
    /**
     * 统计关联的智能体数量
     */
    int countRelatedAgents(String id);
}
