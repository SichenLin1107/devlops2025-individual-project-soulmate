package com.soulmate.module.knowledge.service;

import com.soulmate.common.BusinessException;
import com.soulmate.module.knowledge.entity.AiKnowledgeBase;
import com.soulmate.module.knowledge.entity.AiKnowledgeDocument;
import com.soulmate.module.knowledge.entity.AiKnowledgeSegment;
import com.soulmate.module.knowledge.mapper.AiKnowledgeBaseMapper;
import com.soulmate.module.knowledge.mapper.AiKnowledgeDocumentMapper;
import com.soulmate.module.knowledge.mapper.AiKnowledgeSegmentMapper;
import com.soulmate.util.TextSegmentUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 文档处理服务，负责异步处理文档的切片、向量化和存储
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentProcessor {
    
    private final AiKnowledgeDocumentMapper documentMapper;
    private final AiKnowledgeSegmentMapper segmentMapper;
    private final AiKnowledgeBaseMapper kbMapper;
    private final RagClient ragClient;
    
    @Value("${knowledge.segment.chunk-size:200}")
    private int chunkSize;
    
    @Value("${knowledge.segment.chunk-overlap:50}")
    private int chunkOverlap;
    
    /**
     * 异步处理文档：读取文件、切片、向量化、存储
     */
    @Async("taskExecutor")
    public void processDocumentAsync(Long docId) {
        try {
            AiKnowledgeDocument document = documentMapper.selectById(docId);
            if (document == null) {
                log.error("文档不存在，无法处理: docId={}", docId);
                return;
            }
            
            try {
                updateDocumentStatus(docId, "processing", null);
            } catch (Exception e) {
                log.error("更新文档状态为处理中失败: docId={}", docId, e);
            }
            
            String content;
            try {
                content = readFileContent(document.getFilePath(), document.getFileType());
                if (content == null || content.trim().isEmpty()) {
                    safeUpdateDocumentStatus(docId, "failed", "文件内容为空");
                    return;
                }
            } catch (Exception e) {
                log.error("读取文件失败: docId={}, error={}", docId, e.getMessage());
                safeUpdateDocumentStatus(docId, "failed", "读取文件失败: " + e.getMessage());
                return;
            }
            
            List<String> segments;
            try {
                segments = TextSegmentUtil.segmentText(content, chunkSize, chunkOverlap);
                if (segments.isEmpty()) {
                    safeUpdateDocumentStatus(docId, "failed", "文本切片失败：切片结果为空");
                    return;
                }
            } catch (Exception e) {
                log.error("文本切片失败: docId={}, error={}", docId, e.getMessage());
                safeUpdateDocumentStatus(docId, "failed", "文本切片异常: " + e.getMessage());
                return;
            }
            
            int indexedCount = 0;
            try {
                indexedCount = ragClient.indexDocument(document.getKbId(), docId, segments);
                if (indexedCount <= 0 || indexedCount != segments.size()) {
                    String errorMsg = String.format("向量索引数量不匹配: 期望=%d, 实际=%d", 
                            segments.size(), indexedCount);
                    log.error("向量索引数量验证失败: docId={}, {}", docId, errorMsg);
                    safeUpdateDocumentStatus(docId, "failed", errorMsg);
                    return;
                }
            } catch (BusinessException e) {
                // 捕获BusinessException以获取详细的错误代码
                log.error("RAG服务索引失败: docId={}, errorCode={}, error={}", docId, e.getCode(), e.getMessage());
                String errorMsg = formatErrorMessage(e.getCode(), e.getMessage());
                safeUpdateDocumentStatus(docId, "failed", errorMsg);
                return;
            } catch (Exception e) {
                log.error("RAG服务索引失败: docId={}, error={}", docId, e.getMessage());
                safeUpdateDocumentStatus(docId, "failed", "RAG服务向量数据库索引失败: " + e.getMessage());
                return;
            }
            
            try {
                saveSegmentsToDatabase(document.getKbId(), docId, segments);
            } catch (Exception e) {
                log.error("保存切片到MySQL失败: docId={}, error={}", docId, e.getMessage());
                try {
                    ragClient.deleteDocument(document.getKbId(), docId);
                } catch (Exception rollbackEx) {
                    log.error("向量数据库回滚失败: docId={}, error={}", docId, rollbackEx.getMessage());
                }
                safeUpdateDocumentStatus(docId, "failed", "保存切片到MySQL失败: " + e.getMessage());
                return;
            }
            
            try {
                updateDocumentSegmentCount(docId, indexedCount);
                updateDocumentStatus(docId, "completed", null);
            } catch (Exception e) {
                log.error("更新文档状态失败: docId={}, error={}", docId, e.getMessage());
                safeUpdateDocumentStatus(docId, "failed", "更新文档状态失败: " + e.getMessage());
                try {
                    ragClient.deleteDocument(document.getKbId(), docId);
                    segmentMapper.softDeleteByDocId(docId);
                } catch (Exception rollbackEx) {
                    log.error("回滚失败: docId={}, error={}", docId, rollbackEx.getMessage());
                }
                return;
            }
            
            try {
                updateKnowledgeBaseStats(document.getKbId(), 0, indexedCount);
            } catch (Exception e) {
                log.error("更新知识库统计失败: kbId={}, docId={}, error={}", document.getKbId(), docId, e.getMessage());
            }
            
            log.info("文档处理成功: docId={}, segments={}", docId, indexedCount);
        } catch (Exception e) {
            log.error("文档处理异常: docId={}, error={}", docId, e.getMessage(), e);
            safeUpdateDocumentStatus(docId, "failed", "处理异常: " + e.getMessage());
        }
    }
    
    /**
     * 读取文件内容
     */
    private String readFileContent(String filePath, String fileType) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("文件不存在: " + filePath);
        }
        
        switch (fileType.toLowerCase()) {
            case "txt":
            case "md":
                return Files.readString(path);
            case "pdf":
                throw new UnsupportedOperationException("暂不支持PDF文件格式");
            case "doc":
            case "docx":
                throw new UnsupportedOperationException("暂不支持Word文档格式");
            default:
                throw new UnsupportedOperationException("不支持的文件格式: " + fileType);
        }
    }
    
    /**
     * 保存切片到数据库
     */
    private void saveSegmentsToDatabase(String kbId, Long docId, List<String> segments) {
        List<AiKnowledgeSegment> segmentList = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++) {
            AiKnowledgeSegment segment = new AiKnowledgeSegment();
            segment.setKbId(kbId);
            segment.setDocId(docId);
            segment.setContent(segments.get(i));
            segment.setVectorId(docId + "_" + i);
            segment.setWordCount(segments.get(i).length());
            segment.setPosition(i);
            segment.setStatus(1);
            segmentList.add(segment);
        }
        
        if (!segmentList.isEmpty()) {
            segmentMapper.batchInsert(segmentList);
        }
    }
    
    /**
     * 更新文档状态和错误信息
     */
    private void updateDocumentStatus(Long docId, String status, String errorMessage) {
        try {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                AiKnowledgeDocument document = documentMapper.selectById(docId);
                int retryCount = document != null && document.getRetryCount() != null ? document.getRetryCount() : 0;
                documentMapper.updateRetry(docId, status, retryCount, errorMessage);
            } else {
                documentMapper.updateStatus(docId, status);
            }
        } catch (Exception e) {
            log.error("更新文档状态失败: docId={}, status={}, error={}", docId, status, e.getMessage());
            throw e;
        }
    }
    
    /**
     * 安全更新文档状态（捕获异常不抛出）
     */
    private void safeUpdateDocumentStatus(Long docId, String status, String errorMessage) {
        try {
            updateDocumentStatus(docId, status, errorMessage);
        } catch (Exception e) {
            log.error("安全更新文档状态失败: docId={}, status={}, errorMessage={}", 
                    docId, status, errorMessage, e);
        }
    }
    
    /**
     * 更新文档切片数量
     */
    private void updateDocumentSegmentCount(Long docId, int segmentCount) {
        documentMapper.updateSegmentCount(docId, segmentCount);
    }
    
    /**
     * 更新知识库统计信息（文档数和切片数）
     */
    private void updateKnowledgeBaseStats(String kbId, int docCountIncrement, int segmentCountIncrement) {
        AiKnowledgeBase kb = kbMapper.selectById(kbId);
        if (kb == null) {
            return;
        }
        
        int currentDocCount = (kb.getDocCount() != null ? kb.getDocCount() : 0);
        int currentSegmentCount = (kb.getSegmentCount() != null ? kb.getSegmentCount() : 0);
        int newDocCount = Math.max(0, currentDocCount + docCountIncrement);
        int newSegmentCount = Math.max(0, currentSegmentCount + segmentCountIncrement);
        
        kbMapper.updateCounts(kbId, newDocCount, newSegmentCount);
    }
    
    /**
     * 格式化错误消息，包含错误代码便于前端解析
     * @param errorCode 错误代码
     * @param message 错误消息
     * @return 格式化的错误消息，格式: [ERROR_CODE] 错误消息
     */
    private String formatErrorMessage(int errorCode, String message) {
        // 格式: [5009] Embedding API认证失败，请检查API Key配置
        return String.format("[%d] %s", errorCode, message != null ? message : "未知错误");
    }
}
