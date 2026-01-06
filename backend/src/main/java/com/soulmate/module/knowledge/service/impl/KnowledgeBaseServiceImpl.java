package com.soulmate.module.knowledge.service.impl;

import com.soulmate.common.BusinessException;
import com.soulmate.common.ErrorCode;
import com.soulmate.common.PageResult;
import com.soulmate.module.agent.entity.AiAgent;
import com.soulmate.module.agent.mapper.AiAgentKnowledgeMapper;
import com.soulmate.module.agent.mapper.AiAgentMapper;
import com.soulmate.module.knowledge.dto.*;
import com.soulmate.module.knowledge.entity.AiKnowledgeBase;
import com.soulmate.module.knowledge.entity.AiKnowledgeDocument;
import com.soulmate.module.knowledge.mapper.AiKnowledgeBaseMapper;
import com.soulmate.module.knowledge.mapper.AiKnowledgeDocumentMapper;
import com.soulmate.module.knowledge.mapper.AiKnowledgeSegmentMapper;
import com.soulmate.module.knowledge.service.DocumentProcessor;
import com.soulmate.module.knowledge.service.KnowledgeBaseService;
import com.soulmate.module.knowledge.service.RagClient;
import com.soulmate.security.UserContext;
import com.soulmate.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 知识库服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {
    
    private static final int MAX_RETRY_COUNT = 3;
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024L;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    
    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_PROCESSING = "processing";
    private static final String STATUS_COMPLETED = "completed";
    private static final String STATUS_FAILED = "failed";
    
    private final AiKnowledgeBaseMapper kbMapper;
    private final AiKnowledgeDocumentMapper documentMapper;
    private final AiKnowledgeSegmentMapper segmentMapper;
    private final DocumentProcessor documentProcessor;
    private final RagClient ragClient;
    private final AiAgentKnowledgeMapper agentKnowledgeMapper;
    private final AiAgentMapper agentMapper;
    
    @Value("${file.upload.knowledge-path}")
    private String knowledgeUploadPath;
    
    @Value("${file.upload.allowed-types}")
    private String allowedTypes;
    
    private Set<String> allowedTypesCache;
    
    /**
     * 获取允许的文件类型集合（懒加载缓存）
     */
    private Set<String> getAllowedTypes() {
        if (allowedTypesCache == null) {
            allowedTypesCache = Arrays.stream(allowedTypes.split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
        }
        return allowedTypesCache;
    }
    
    /**
     * 验证知识库是否存在，不存在则抛出异常
     */
    private AiKnowledgeBase validateKnowledgeBaseExists(String kbId) {
        AiKnowledgeBase kb = kbMapper.selectById(kbId);
        if (kb == null) {
            throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_NOT_FOUND);
        }
        return kb;
    }
    
    /**
     * 验证文档是否存在且属于指定知识库，不存在则抛出异常
     */
    private AiKnowledgeDocument validateDocumentExists(String kbId, Long docId) {
        AiKnowledgeDocument document = documentMapper.selectById(docId);
        if (document == null || !kbId.equals(document.getKbId())) {
            throw new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND);
        }
        return document;
    }
    
    /**
     * 规范化分页参数（原地修改数组）
     */
    private void normalizePageParams(int[] pageSize) {
        if (pageSize[0] < 1) {
            pageSize[0] = 1;
        }
        if (pageSize[1] < 1 || pageSize[1] > MAX_PAGE_SIZE) {
            pageSize[1] = DEFAULT_PAGE_SIZE;
        }
    }
    
    @Override
    public PageResult<KnowledgeBaseVO> listKnowledgeBases(int page, int size, String keyword) {
        int offset = (page - 1) * size;
        List<AiKnowledgeBase> kbs = kbMapper.selectPage(offset, size);
        long total = kbMapper.countAll();
        
        List<KnowledgeBaseVO> list = kbs.stream()
                .map(KnowledgeBaseVO::fromEntity)
                .collect(Collectors.toList());
        
        return PageResult.of(list, total, page, size);
    }
    
    @Override
    public KnowledgeBaseVO getKnowledgeBase(String id) {
        AiKnowledgeBase kb = validateKnowledgeBaseExists(id);
        return KnowledgeBaseVO.fromEntity(kb);
    }
    
    @Override
    public String createKnowledgeBase(KnowledgeBaseRequest request) {
        AiKnowledgeBase kb = new AiKnowledgeBase();
        kb.setId(IdGenerator.knowledgeBaseId());
        kb.setName(request.getName());
        kb.setDescription(request.getDescription());
        kb.setEmbeddingModel(request.getEmbeddingModel());
        kb.setDocCount(0);
        kb.setSegmentCount(0);
        kb.setCreatedBy(UserContext.getUserId());
        
        kbMapper.insert(kb);
        return kb.getId();
    }
    
    @Override
    public void updateKnowledgeBase(String id, KnowledgeBaseRequest request) {
        validateKnowledgeBaseExists(id);
        
        AiKnowledgeBase kb = new AiKnowledgeBase();
        kb.setId(id);
        kb.setName(request.getName());
        kb.setDescription(request.getDescription());
        kb.setEmbeddingModel(request.getEmbeddingModel());
        
        kbMapper.update(kb);
    }
    
    @Override
    @Transactional
    public void deleteKnowledgeBase(String id) {
        validateKnowledgeBaseExists(id);
        
        try {
            ragClient.deleteCollection(id);
        } catch (Exception e) {
            log.error("删除RAG服务向量集合失败: kbId={}, error={}", id, e.getMessage());
        }
        
        segmentMapper.deleteByKbId(id);
        documentMapper.deleteByKbId(id);
        kbMapper.deleteById(id);
    }
    
    @Override
    public PageResult<DocumentVO> listDocuments(String kbId, int page, int size, String status) {
        validateKnowledgeBaseExists(kbId);
        
        int[] pageSize = {page, size};
        normalizePageParams(pageSize);
        page = pageSize[0];
        size = pageSize[1];
        
        int offset = (page - 1) * size;
        String statusFilter = (status != null && !status.trim().isEmpty()) ? status.trim() : null;
        
        List<AiKnowledgeDocument> documents = documentMapper.selectByKbIdWithStatus(
                kbId, statusFilter, offset, size);
        long total = documentMapper.countByKbIdWithStatus(kbId, statusFilter);
        
        List<DocumentVO> list = documents.stream()
                .map(DocumentVO::fromEntity)
                .collect(Collectors.toList());
        
        return PageResult.of(list, total, page, size);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDocument(String kbId, Long docId) {
        AiKnowledgeBase kb = validateKnowledgeBaseExists(kbId);
        AiKnowledgeDocument document = validateDocumentExists(kbId, docId);
        
        int documentSegmentCount = (document.getSegmentCount() != null ? document.getSegmentCount() : 0);
        String fileName = document.getFileName();
        
        try {
            ragClient.deleteDocument(kbId, docId);
        } catch (Exception e) {
            log.error("删除RAG服务文档向量失败: kbId={}, docId={}, error={}", kbId, docId, e.getMessage());
            throw new BusinessException(ErrorCode.RAG_SERVICE_ERROR, "删除向量数据失败: " + e.getMessage());
        }
        
        try {
            segmentMapper.softDeleteByDocId(docId);
        } catch (Exception e) {
            log.error("删除MySQL切片记录失败: kbId={}, docId={}, error={}", kbId, docId, e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除切片记录失败: " + e.getMessage());
        }
        
        try {
            documentMapper.deleteById(docId);
        } catch (Exception e) {
            log.error("删除MySQL文档记录失败: kbId={}, docId={}, error={}", kbId, docId, e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除文档记录失败: " + e.getMessage());
        }
        
        try {
            int currentDocCount = (kb.getDocCount() != null ? kb.getDocCount() : 0);
            int currentSegmentCount = (kb.getSegmentCount() != null ? kb.getSegmentCount() : 0);
            int newDocCount = Math.max(0, currentDocCount - 1);
            int newSegmentCount = Math.max(0, currentSegmentCount - documentSegmentCount);
            kbMapper.updateCounts(kbId, newDocCount, newSegmentCount);
        } catch (Exception e) {
            log.error("更新知识库统计失败: kbId={}, error={}", kbId, e.getMessage());
        }
        
        try {
            if (document.getFilePath() != null) {
                Path filePath = Paths.get(document.getFilePath());
                Files.deleteIfExists(filePath);
            }
        } catch (Exception e) {
            log.warn("删除文档文件失败: {}, error={}", document.getFilePath(), e.getMessage());
        }
        
        log.info("文档删除成功: kbId={}, docId={}, fileName={}", kbId, docId, fileName);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long uploadDocument(String kbId, MultipartFile file) {
        AiKnowledgeBase kb = validateKnowledgeBaseExists(kbId);
        
        validateFile(file);
        String filePath = saveFile(kbId, file);
        
        AiKnowledgeDocument document = new AiKnowledgeDocument();
        document.setKbId(kbId);
        document.setFileName(file.getOriginalFilename());
        document.setFileType(getFileExtension(file.getOriginalFilename()));
        document.setFileSize((int) file.getSize());
        document.setFilePath(filePath);
        document.setSegmentCount(0);
        document.setStatus(STATUS_PENDING);
        document.setCreatedAt(LocalDateTime.now());
        
        documentMapper.insert(document);
        
        int currentDocCount = (kb.getDocCount() != null ? kb.getDocCount() : 0);
        int currentSegmentCount = (kb.getSegmentCount() != null ? kb.getSegmentCount() : 0);
        kbMapper.updateCounts(kbId, currentDocCount + 1, currentSegmentCount);
        
        Long docId = document.getId();
        submitDocumentProcessingTask(kbId, docId, 0);
        
        return docId;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retryDocument(String kbId, Long docId) {
        validateKnowledgeBaseExists(kbId);
        AiKnowledgeDocument document = validateDocumentExists(kbId, docId);
        
        if (!STATUS_FAILED.equals(document.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "只能重试失败的文档");
        }
        
        int retryCount = (document.getRetryCount() != null ? document.getRetryCount() : 0);
        if (retryCount >= MAX_RETRY_COUNT) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION, 
                    String.format("文档重试次数已达上限(%d次)", MAX_RETRY_COUNT));
        }
        
        documentMapper.updateRetry(docId, STATUS_PENDING, retryCount + 1, null);
        submitDocumentProcessingTask(kbId, docId, retryCount + 1);
    }
    
    @Override
    public List<RetrievalResult> testRetrieval(String kbId, RetrievalTestRequest request) {
        validateKnowledgeBaseExists(kbId);
        return ragClient.search(kbId, request.getQuery(), request.getTopK());
    }
    
    @Override
    public Map<String, Object> getDocumentStatus(String kbId, Long docId) {
        validateKnowledgeBaseExists(kbId);
        AiKnowledgeDocument document = validateDocumentExists(kbId, docId);
        
        Map<String, Object> status = new HashMap<>();
        status.put("docId", document.getId());
        status.put("fileName", document.getFileName());
        status.put("status", document.getStatus());
        status.put("segmentCount", document.getSegmentCount());
        status.put("fileSize", document.getFileSize());
        status.put("errorMessage", document.getErrorMessage());
        status.put("createdAt", document.getCreatedAt());
        status.put("updatedAt", document.getUpdatedAt());
        status.put("statusDescription", getStatusDescription(document.getStatus()));
        
        if (STATUS_FAILED.equals(document.getStatus())) {
            status.put("canRetry", true);
            status.put("retryCount", document.getRetryCount() != null ? document.getRetryCount() : 0);
        } else {
            status.put("canRetry", false);
        }
        
        return status;
    }
    
    /**
     * 验证上传文件
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "文件不能为空");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "文件名不能为空");
        }
        
        String fileExtension = getFileExtension(filename);
        if (!getAllowedTypes().contains(fileExtension)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, 
                    "不支持的文件格式，支持的格式: " + allowedTypes);
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, 
                    String.format("文件大小不能超过%dMB", MAX_FILE_SIZE / 1024 / 1024));
        }
    }
    
    /**
     * 保存文件
     */
    private String saveFile(String kbId, MultipartFile file) {
        try {
            Path uploadDir = Paths.get(knowledgeUploadPath, kbId);
            Files.createDirectories(uploadDir);
            
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadDir.resolve(filename);
            file.transferTo(filePath);
            
            return filePath.toString();
        } catch (IOException e) {
            log.error("文件保存失败: kbId={}, filename={}", kbId, file.getOriginalFilename(), e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "文件保存失败");
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
    
    /**
     * 提交文档处理异步任务（事务提交后执行）
     */
    private void submitDocumentProcessingTask(String kbId, Long docId, int retryCount) {
        Runnable task = () -> {
            try {
                documentProcessor.processDocumentAsync(docId);
            } catch (Exception e) {
                log.error("提交异步处理任务失败: kbId={}, docId={}, error={}", kbId, docId, e.getMessage());
                try {
                    documentMapper.updateRetry(docId, STATUS_FAILED, retryCount, 
                            "异步任务提交失败: " + e.getMessage());
                } catch (Exception ex) {
                    log.error("更新文档状态为失败时发生异常: docId={}", docId, ex);
                }
            }
        };
        
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }
    
    /**
     * 获取状态描述
     */
    private String getStatusDescription(String status) {
        return switch (status) {
            case STATUS_PENDING -> "等待处理";
            case STATUS_PROCESSING -> "处理中";
            case STATUS_COMPLETED -> "处理完成";
            case STATUS_FAILED -> "处理失败";
            default -> "未知状态";
        };
    }
    
    @Override
    @Transactional
    public void updateKnowledgeBaseStatus(String id, Integer isActive, boolean disableRelatedAgents) {
        validateKnowledgeBaseExists(id);
        
        if (isActive != null && isActive == 0 && disableRelatedAgents) {
            List<com.soulmate.module.agent.entity.AiAgentKnowledge> agentKnowledgeList = 
                    agentKnowledgeMapper.selectByKbId(id);
            if (!agentKnowledgeList.isEmpty()) {
                List<String> agentIds = agentKnowledgeList.stream()
                        .map(com.soulmate.module.agent.entity.AiAgentKnowledge::getAgentId)
                        .distinct()
                        .collect(Collectors.toList());
                agentMapper.batchUpdateStatus(agentIds, "offline");
                log.info("禁用知识库时，同时禁用了 {} 个关联的智能体: kbId={}, agentIds={}", 
                        agentIds.size(), id, agentIds);
            }
        }
        
        kbMapper.updateStatus(id, isActive);
        log.info("知识库状态更新: id={}, isActive={}", id, isActive);
    }
    
    @Override
    public int countRelatedAgents(String id) {
        List<com.soulmate.module.agent.entity.AiAgentKnowledge> agentKnowledgeList = agentKnowledgeMapper.selectByKbId(id);
        return (int) agentKnowledgeList.stream()
                .map(com.soulmate.module.agent.entity.AiAgentKnowledge::getAgentId)
                .distinct()
                .count();
    }
}

