package com.soulmate.module.knowledge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soulmate.common.BusinessException;
import com.soulmate.common.ErrorCode;
import com.soulmate.module.knowledge.dto.RetrievalResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG服务客户端，负责与向量数据库服务交互
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagClient {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${rag.service.base-url}")
    private String ragServiceBaseUrl;
    
    /**
     * 根据RAG服务返回的错误类型获取对应的ErrorCode
     */
    private ErrorCode getErrorCodeByType(String errorType) {
        if (errorType == null) {
            return ErrorCode.RAG_SERVICE_ERROR;
        }
        return switch (errorType) {
            case "AUTH_ERROR" -> ErrorCode.RAG_API_AUTH_ERROR;
            case "QUOTA_ERROR" -> ErrorCode.RAG_API_QUOTA_ERROR;
            case "RATE_LIMIT" -> ErrorCode.RAG_API_RATE_LIMIT;
            case "TIMEOUT" -> ErrorCode.RAG_API_TIMEOUT;
            case "MODEL_ERROR" -> ErrorCode.RAG_EMBEDDING_ERROR;
            case "CONNECTION_ERROR" -> ErrorCode.RAG_SERVICE_UNAVAILABLE;
            default -> ErrorCode.RAG_SERVICE_ERROR;
        };
    }
    
    /**
     * 解析RAG服务的错误响应
     */
    @SuppressWarnings("unchecked")
    private BusinessException parseRagError(String responseBody, String defaultMessage) {
        try {
            Map<String, Object> errorDetail = objectMapper.readValue(responseBody, Map.class);
            String message = (String) errorDetail.getOrDefault("message", defaultMessage);
            String errorType = (String) errorDetail.get("error_type");
            
            ErrorCode errorCode = getErrorCodeByType(errorType);
            log.error("RAG服务错误: errorType={}, message={}", errorType, message);
            return new BusinessException(errorCode, message);
        } catch (Exception e) {
            log.warn("解析RAG错误响应失败: {}", e.getMessage());
            return new BusinessException(ErrorCode.RAG_SERVICE_ERROR, defaultMessage);
        }
    }
    
    /**
     * 将文档切片索引到向量数据库
     */
    public int indexDocument(String kbId, Long docId, List<String> segments) {
        String url = ragServiceBaseUrl + "/api/rag/index";
        
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("kb_id", kbId);
            requestBody.put("doc_id", docId);
            requestBody.put("segments", segments);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Boolean success = (Boolean) responseBody.get("success");
                Integer count = (Integer) responseBody.get("count");
                
                if (success != null && success && count != null) {
                    log.info("RAG服务索引成功: kbId={}, docId={}, count={}", kbId, docId, count);
                    return count;
                } else {
                    String message = (String) responseBody.get("message");
                    log.error("RAG服务索引失败: kbId={}, docId={}, message={}", kbId, docId, message);
                    throw new BusinessException(ErrorCode.RAG_SERVICE_ERROR, "RAG服务索引失败: " + message);
                }
            } else {
                log.error("RAG服务响应异常: kbId={}, docId={}, statusCode={}", kbId, docId, response.getStatusCode());
                throw new BusinessException(ErrorCode.RAG_SERVICE_ERROR, "RAG服务响应异常: " + response.getStatusCode());
            }
            
        } catch (BusinessException e) {
            throw e;
        } catch (HttpClientErrorException e) {
            log.error("RAG服务调用失败(客户端错误): kbId={}, docId={}, error={}", kbId, docId, e.getMessage());
            throw new BusinessException(ErrorCode.RAG_SERVICE_ERROR, "RAG服务调用失败(客户端错误): " + e.getMessage());
        } catch (HttpServerErrorException e) {
            log.error("RAG服务调用失败(服务端错误): kbId={}, docId={}, error={}", kbId, docId, e.getMessage());
            // 解析RAG服务返回的错误类型
            ErrorCode errorCode = parseRagErrorType(e.getResponseBodyAsString());
            String errorMsg = parseRagErrorMessage(e.getResponseBodyAsString());
            throw new BusinessException(errorCode, errorMsg);
        } catch (org.springframework.web.client.ResourceAccessException e) {
            if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                log.error("RAG服务调用超时: kbId={}, docId={}, error={}", kbId, docId, e.getMessage());
                throw new BusinessException(ErrorCode.RAG_API_TIMEOUT, "RAG服务调用超时，请检查网络或RAG服务状态");
            } else {
                log.error("RAG服务连接失败: kbId={}, docId={}, error={}", kbId, docId, e.getMessage());
                throw new BusinessException(ErrorCode.RAG_SERVICE_UNAVAILABLE, "无法连接到RAG服务: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("RAG服务调用异常: kbId={}, docId={}, error={}", kbId, docId, e.getMessage());
            throw new BusinessException(ErrorCode.RAG_SERVICE_ERROR, "调用RAG服务异常: " + e.getMessage());
        }
    }
    
    /**
     * 解析RAG服务返回的错误类型
     */
    private ErrorCode parseRagErrorType(String responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            return ErrorCode.RAG_SERVICE_ERROR;
        }
        
        try {
            // 尝试解析JSON响应
            if (responseBody.contains("AUTH_ERROR") || responseBody.contains("401") || 
                responseBody.contains("令牌") || responseBody.contains("authentication")) {
                return ErrorCode.RAG_API_AUTH_ERROR;
            } else if (responseBody.contains("QUOTA_ERROR") || responseBody.contains("402") || 
                       responseBody.contains("余额") || responseBody.contains("额度")) {
                return ErrorCode.RAG_API_QUOTA_ERROR;
            } else if (responseBody.contains("RATE_LIMIT") || responseBody.contains("429") || 
                       responseBody.contains("频率")) {
                return ErrorCode.RAG_API_RATE_LIMIT;
            } else if (responseBody.contains("TIMEOUT") || responseBody.contains("超时")) {
                return ErrorCode.RAG_API_TIMEOUT;
            } else if (responseBody.contains("CONNECTION_ERROR") || responseBody.contains("连接")) {
                return ErrorCode.RAG_SERVICE_UNAVAILABLE;
            }
        } catch (Exception e) {
            log.warn("解析RAG错误类型失败: {}", e.getMessage());
        }
        
        return ErrorCode.RAG_EMBEDDING_ERROR;
    }
    
    /**
     * 解析RAG服务返回的错误消息
     */
    private String parseRagErrorMessage(String responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            return "RAG服务调用失败";
        }
        
        try {
            // 尝试从JSON中提取message字段
            if (responseBody.contains("\"message\"")) {
                int start = responseBody.indexOf("\"message\"");
                int colonIdx = responseBody.indexOf(":", start);
                int quoteStart = responseBody.indexOf("\"", colonIdx + 1);
                int quoteEnd = responseBody.indexOf("\"", quoteStart + 1);
                if (quoteStart > 0 && quoteEnd > quoteStart) {
                    return responseBody.substring(quoteStart + 1, quoteEnd);
                }
            }
            // 如果是detail字符串格式
            if (responseBody.contains("\"detail\"")) {
                int start = responseBody.indexOf("\"detail\"");
                int colonIdx = responseBody.indexOf(":", start);
                // 检查是否是对象格式 {"detail": {"message": ...}}
                int braceIdx = responseBody.indexOf("{", colonIdx);
                int quoteIdx = responseBody.indexOf("\"", colonIdx + 1);
                if (braceIdx > 0 && (quoteIdx < 0 || braceIdx < quoteIdx)) {
                    // 对象格式，递归提取message
                    String innerJson = responseBody.substring(braceIdx);
                    return parseRagErrorMessage(innerJson);
                } else if (quoteIdx > 0) {
                    // 字符串格式
                    int quoteEnd = responseBody.indexOf("\"", quoteIdx + 1);
                    if (quoteEnd > quoteIdx) {
                        return responseBody.substring(quoteIdx + 1, quoteEnd);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析RAG错误消息失败: {}", e.getMessage());
        }
        
        return responseBody.length() > 200 ? responseBody.substring(0, 200) : responseBody;
    }
    
    /**
     * 向量相似度检索
     */
    @SuppressWarnings("unchecked")
    public List<RetrievalResult> search(String kbId, String query, int topK) {
        try {
            String url = ragServiceBaseUrl + "/api/rag/search";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("kb_id", kbId);
            requestBody.put("query", query);
            requestBody.put("top_k", topK);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> results = (List<Map<String, Object>>) responseBody.get("results");
                
                if (results != null) {
                    List<RetrievalResult> retrievalResults = results.stream()
                            .map(this::mapToRetrievalResult)
                            .toList();
                    
                    log.info("RAG服务检索成功: kbId={}, count={}", kbId, retrievalResults.size());
                    return retrievalResults;
                } else {
                    // 如果results为null，返回空列表而不是抛出异常（可能是知识库为空）
                    log.warn("RAG服务检索结果为空: kbId={}, query={}", kbId, query);
                    return new ArrayList<>();
                }
            } else {
                log.error("RAG服务响应异常: kbId={}, statusCode={}", kbId, response.getStatusCode());
                throw new BusinessException(ErrorCode.RAG_SERVICE_ERROR, "RAG服务响应异常: " + response.getStatusCode());
            }
            
        } catch (BusinessException e) {
            throw e;
        } catch (HttpClientErrorException e) {
            log.error("RAG服务调用失败(客户端错误): kbId={}, query={}, error={}", kbId, query, e.getMessage());
            ErrorCode errorCode = parseRagErrorType(e.getResponseBodyAsString());
            String errorMsg = parseRagErrorMessage(e.getResponseBodyAsString());
            throw new BusinessException(errorCode, errorMsg);
        } catch (HttpServerErrorException e) {
            log.error("RAG服务调用失败(服务端错误): kbId={}, query={}, error={}", kbId, query, e.getMessage());
            ErrorCode errorCode = parseRagErrorType(e.getResponseBodyAsString());
            String errorMsg = parseRagErrorMessage(e.getResponseBodyAsString());
            throw new BusinessException(errorCode, errorMsg);
        } catch (org.springframework.web.client.ResourceAccessException e) {
            if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                log.error("RAG服务调用超时: kbId={}, query={}, error={}", kbId, query, e.getMessage());
                throw new BusinessException(ErrorCode.RAG_API_TIMEOUT, "RAG服务调用超时，请检查网络或RAG服务状态");
            } else {
                log.error("RAG服务连接失败: kbId={}, query={}, error={}", kbId, query, e.getMessage());
                throw new BusinessException(ErrorCode.RAG_SERVICE_UNAVAILABLE, "无法连接到RAG服务，请检查服务是否正常运行");
            }
        } catch (Exception e) {
            log.error("RAG服务检索失败: kbId={}, query={}, error={}", kbId, query, e.getMessage(), e);
            throw new BusinessException(ErrorCode.RAG_SERVICE_ERROR, "调用RAG服务异常: " + e.getMessage());
        }
    }
    
    /**
     * 删除知识库的向量集合
     */
    public void deleteCollection(String kbId) {
        try {
            String url = ragServiceBaseUrl + "/api/rag/collection/" + kbId;
            restTemplate.delete(url);
            log.info("RAG服务删除集合成功: kbId={}", kbId);
        } catch (Exception e) {
            log.error("RAG服务删除集合失败: kbId={}, error={}", kbId, e.getMessage());
            throw new BusinessException(ErrorCode.RAG_SERVICE_ERROR, "调用RAG服务异常: " + e.getMessage());
        }
    }
    
    /**
     * 删除文档的向量数据
     */
    public void deleteDocument(String kbId, Long docId) {
        try {
            String url = ragServiceBaseUrl + "/api/rag/document/" + kbId + "/" + docId;
            restTemplate.delete(url);
            log.info("RAG服务删除文档成功: kbId={}, docId={}", kbId, docId);
        } catch (Exception e) {
            log.error("RAG服务删除文档失败: kbId={}, docId={}, error={}", kbId, docId, e.getMessage());
            throw new BusinessException(ErrorCode.RAG_SERVICE_ERROR, "调用RAG服务异常: " + e.getMessage());
        }
    }
    
    /**
     * 健康检查
     */
    public boolean isHealthy() {
        try {
            String url = ragServiceBaseUrl + "/api/rag/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String status = (String) response.getBody().get("status");
                return "healthy".equals(status);
            }
            return false;
            
        } catch (Exception e) {
            log.warn("RAG服务健康检查失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 将响应结果映射为RetrievalResult对象
     */
    @SuppressWarnings("unchecked")
    private RetrievalResult mapToRetrievalResult(Map<String, Object> result) {
        RetrievalResult retrievalResult = new RetrievalResult();
        retrievalResult.setText((String) result.get("text"));
        
        Object scoreObj = result.get("score");
        if (scoreObj instanceof Number) {
            retrievalResult.setScore(((Number) scoreObj).doubleValue());
        }
        
        Object docIdObj = result.get("doc_id");
        if (docIdObj instanceof Number) {
            retrievalResult.setDocId(((Number) docIdObj).longValue());
        }
        
        retrievalResult.setSegmentId((String) result.get("segment_id"));
        retrievalResult.setMetadata((Map<String, Object>) result.get("metadata"));
        
        return retrievalResult;
    }
}
