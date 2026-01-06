package com.soulmate.module.llm.service.impl;

import com.soulmate.common.BusinessException;
import com.soulmate.common.ErrorCode;
import com.soulmate.module.llm.dto.ChatMessage;
import com.soulmate.module.llm.entity.LlmModel;
import com.soulmate.module.llm.entity.LlmProvider;
import com.soulmate.module.llm.mapper.LlmModelMapper;
import com.soulmate.module.llm.mapper.LlmProviderMapper;
import com.soulmate.module.llm.service.LlmClient;
import com.soulmate.util.EncryptionUtil;
import com.soulmate.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * LLM统一调用客户端实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmClientImpl implements LlmClient {
    
    private final LlmModelMapper modelMapper;
    private final LlmProviderMapper providerMapper;
    private final RestTemplate restTemplate;
    private final EncryptionUtil encryptionUtil;
    
    @Override
    public String chat(String modelId, List<ChatMessage> messages, Map<String, Object> configOverride) {
        LlmModel model = modelMapper.selectById(modelId);
        if (model == null) {
            throw new BusinessException(ErrorCode.LLM_MODEL_NOT_FOUND);
        }
        
        if (model.getIsActive() == null || model.getIsActive() == 0) {
            throw new BusinessException(ErrorCode.LLM_CALL_ERROR, "模型已禁用");
        }
        
        LlmProvider provider = providerMapper.selectById(model.getProviderId());
        if (provider == null) {
            throw new BusinessException(ErrorCode.LLM_PROVIDER_NOT_FOUND);
        }
        
        if (provider.getIsActive() == null || provider.getIsActive() == 0) {
            throw new BusinessException(ErrorCode.LLM_CALL_ERROR, "提供商已禁用");
        }
        
        String apiBase = model.getApiBase() != null && !model.getApiBase().isEmpty() 
                ? model.getApiBase() 
                : provider.getApiBase();
        
        if (apiBase == null || apiBase.isEmpty()) {
            throw new BusinessException(ErrorCode.LLM_CALL_ERROR, "API地址未配置");
        }
        
        if (!apiBase.endsWith("/")) {
            apiBase = apiBase + "/";
        }
        
        String encryptedApiKey = model.getApiKey() != null && !model.getApiKey().isEmpty() 
                ? model.getApiKey() 
                : provider.getApiKey();
        
        if (encryptedApiKey == null || encryptedApiKey.isEmpty()) {
            throw new BusinessException(ErrorCode.LLM_CALL_ERROR, "API密钥未配置");
        }
        
        String apiKey;
        try {
            apiKey = encryptionUtil.decrypt(encryptedApiKey);
        } catch (Exception e) {
            log.error("API Key解密失败: modelId={}, providerId={}, error={}", modelId, provider.getId(), e.getMessage());
            throw new BusinessException(ErrorCode.LLM_CALL_ERROR, "API Key解密失败");
        }
        
        Map<String, Object> defaultConfig = JsonUtil.parseMap(model.getDefaultConfig());
        Map<String, Object> finalConfig = new HashMap<>();
        
        if (defaultConfig != null) {
            finalConfig.putAll(defaultConfig);
        }
        
        if (configOverride != null) {
            finalConfig.putAll(configOverride);
        }
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model.getName());
        requestBody.put("messages", convertMessages(messages));
        requestBody.putAll(finalConfig);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        
        String apiUrl = apiBase + "chat/completions";
        log.info("调用LLM API: url={}, model={}", apiUrl, model.getName());
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );
            
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                log.error("LLM API调用失败: status={}, body={}", response.getStatusCode(), response.getBody());
                throw new BusinessException(ErrorCode.LLM_CALL_ERROR, "LLM API返回错误状态");
            }
            
            Map<String, Object> body = response.getBody();
            List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
            
            if (choices == null || choices.isEmpty()) {
                log.error("LLM API返回空响应: body={}", body);
                throw new BusinessException(ErrorCode.LLM_CALL_ERROR, "LLM API返回空响应");
            }
            
            Map<String, Object> firstChoice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            
            if (message == null) {
                log.error("LLM API响应格式错误: choice={}", firstChoice);
                throw new BusinessException(ErrorCode.LLM_CALL_ERROR, "LLM API响应格式错误");
            }
            
            String content = (String) message.get("content");
            
            if (content == null) {
                log.error("LLM API返回内容为空: message={}", message);
                throw new BusinessException(ErrorCode.LLM_CALL_ERROR, "LLM API返回内容为空");
            }
            
            log.info("LLM API调用成功: model={}, contentLength={}", model.getName(), content.length());
            return content;
            
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("LLM API调用失败(客户端错误): url={}, status={}, error={}", apiUrl, e.getStatusCode(), e.getMessage());
            throw parseLlmApiError(e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            log.error("LLM API调用失败(服务端错误): url={}, status={}, error={}", apiUrl, e.getStatusCode(), e.getMessage());
            throw parseLlmApiError(e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (org.springframework.web.client.ResourceAccessException e) {
            if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                log.error("LLM API调用超时: url={}, error={}", apiUrl, e.getMessage());
                throw new BusinessException(ErrorCode.LLM_API_TIMEOUT, "LLM API调用超时，请稍后重试");
            } else {
                log.error("LLM API连接失败: url={}, error={}", apiUrl, e.getMessage());
                throw new BusinessException(ErrorCode.LLM_CALL_ERROR, "无法连接到LLM API: " + e.getMessage());
            }
        } catch (RestClientException e) {
            log.error("LLM API调用异常: url={}, error={}", apiUrl, e.getMessage(), e);
            throw new BusinessException(ErrorCode.LLM_CALL_ERROR, "LLM API调用失败: " + e.getMessage());
        }
    }
    
    /**
     * 解析LLM API错误响应，返回对应的业务异常
     */
    private BusinessException parseLlmApiError(int statusCode, String responseBody) {
        String errorMessage = "LLM API调用失败";
        
        // 尝试从响应体中提取错误信息
        if (responseBody != null && !responseBody.isEmpty()) {
            try {
                // 尝试提取 message 字段
                if (responseBody.contains("\"message\"")) {
                    int start = responseBody.indexOf("\"message\"");
                    int colonIdx = responseBody.indexOf(":", start);
                    int quoteStart = responseBody.indexOf("\"", colonIdx + 1);
                    int quoteEnd = responseBody.indexOf("\"", quoteStart + 1);
                    if (quoteStart > 0 && quoteEnd > quoteStart) {
                        errorMessage = responseBody.substring(quoteStart + 1, quoteEnd);
                    }
                }
            } catch (Exception e) {
                log.warn("解析LLM API错误响应失败: {}", e.getMessage());
            }
        }
        
        // 根据状态码和响应内容判断具体错误类型
        ErrorCode errorCode = switch (statusCode) {
            case 401 -> ErrorCode.LLM_API_AUTH_ERROR;
            case 402, 403 -> {
                // 检查是否是额度问题
                if (responseBody != null && (responseBody.contains("余额") || responseBody.contains("额度") || 
                    responseBody.contains("quota") || responseBody.contains("balance"))) {
                    yield ErrorCode.LLM_API_QUOTA_ERROR;
                }
                yield ErrorCode.LLM_API_AUTH_ERROR;
            }
            case 429 -> ErrorCode.LLM_API_RATE_LIMIT;
            case 404 -> ErrorCode.LLM_API_MODEL_ERROR;
            case 408, 504 -> ErrorCode.LLM_API_TIMEOUT;
            default -> {
                // 进一步检查响应内容
                if (responseBody != null) {
                    if (responseBody.contains("令牌") || responseBody.contains("token") || 
                        responseBody.contains("authentication") || responseBody.contains("认证")) {
                        yield ErrorCode.LLM_API_AUTH_ERROR;
                    } else if (responseBody.contains("余额") || responseBody.contains("额度") || 
                               responseBody.contains("quota")) {
                        yield ErrorCode.LLM_API_QUOTA_ERROR;
                    } else if (responseBody.contains("频率") || responseBody.contains("rate")) {
                        yield ErrorCode.LLM_API_RATE_LIMIT;
                    } else if (responseBody.contains("模型") || responseBody.contains("model")) {
                        yield ErrorCode.LLM_API_MODEL_ERROR;
                    }
                }
                yield ErrorCode.LLM_CALL_ERROR;
            }
        };
        
        return new BusinessException(errorCode, errorMessage);
    }
    
    /**
     * 转换消息格式为API需要的格式
     */
    private List<Map<String, String>> convertMessages(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Map<String, String>> result = new ArrayList<>();
        for (ChatMessage msg : messages) {
            Map<String, String> apiMsg = new HashMap<>();
            apiMsg.put("role", msg.getRole());
            apiMsg.put("content", msg.getContent());
            result.add(apiMsg);
        }
        return result;
    }
}

