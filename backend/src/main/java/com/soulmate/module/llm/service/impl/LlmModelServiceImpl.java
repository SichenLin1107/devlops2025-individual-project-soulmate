package com.soulmate.module.llm.service.impl;

import com.soulmate.common.BusinessException;
import com.soulmate.common.ErrorCode;
import com.soulmate.module.agent.entity.AiAgent;
import com.soulmate.module.agent.mapper.AiAgentMapper;
import com.soulmate.module.llm.dto.LlmModelRequest;
import com.soulmate.module.llm.dto.LlmModelVO;
import com.soulmate.module.llm.dto.TestChatRequest;
import com.soulmate.module.llm.entity.LlmModel;
import com.soulmate.module.llm.entity.LlmProvider;
import com.soulmate.module.llm.mapper.LlmModelMapper;
import com.soulmate.module.llm.mapper.LlmProviderMapper;
import com.soulmate.module.llm.service.LlmModelService;
import com.soulmate.util.EncryptionUtil;
import com.soulmate.util.IdGenerator;
import com.soulmate.util.JsonUtil;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * LLM模型服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmModelServiceImpl implements LlmModelService {
    
    private final LlmModelMapper modelMapper;
    private final LlmProviderMapper providerMapper;
    private final AiAgentMapper agentMapper;
    private final EncryptionUtil encryptionUtil;
    private final RestTemplate restTemplate;
    
    @Override
    public List<LlmModelVO> listModels(String providerId, String modelType, Integer isActive) {
        List<LlmModel> models = modelMapper.selectByCondition(providerId, modelType, isActive);
        
        return models.stream()
                .map(model -> {
                    String decryptedApiKey = null;
                    if (model.getApiKey() != null && !model.getApiKey().isEmpty()) {
                        try {
                            decryptedApiKey = encryptionUtil.decrypt(model.getApiKey());
                        } catch (Exception e) {
                            log.warn("解密API Key失败: modelId={}, error={}", model.getId(), e.getMessage());
                            decryptedApiKey = model.getApiKey();
                        }
                    }
                    return LlmModelVO.fromEntity(model, decryptedApiKey);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public String createModel(LlmModelRequest request) {
        if (providerMapper.selectById(request.getProviderId()) == null) {
            throw new BusinessException(ErrorCode.LLM_PROVIDER_NOT_FOUND);
        }
        
        LlmModel model = new LlmModel();
        model.setId(IdGenerator.modelId());
        model.setProviderId(request.getProviderId());
        model.setName(request.getName());
        model.setDisplayName(request.getDisplayName());
        model.setModelType(request.getModelType());
        model.setApiBase(request.getApiBase());
        
        if (request.getApiKey() != null && !request.getApiKey().isEmpty()) {
            model.setApiKey(encryptionUtil.encrypt(request.getApiKey()));
        }
        
        model.setDefaultConfig(JsonUtil.toJson(request.getDefaultConfig()));
        model.setIsActive(request.getIsActive() != null ? request.getIsActive() : 1);
        
        modelMapper.insert(model);
        log.info("LLM模型创建成功: id={}, name={}", model.getId(), model.getName());
        return model.getId();
    }
    
    @Override
    public void updateModel(String id, LlmModelRequest request) {
        LlmModel model = modelMapper.selectById(id);
        if (model == null) {
            throw new BusinessException(ErrorCode.LLM_MODEL_NOT_FOUND);
        }
        
        if (request.getProviderId() != null && !request.getProviderId().equals(model.getProviderId())) {
            if (providerMapper.selectById(request.getProviderId()) == null) {
                throw new BusinessException(ErrorCode.LLM_PROVIDER_NOT_FOUND);
            }
            model.setProviderId(request.getProviderId());
        }
        
        if (request.getName() != null) {
            model.setName(request.getName());
        }
        if (request.getDisplayName() != null) {
            model.setDisplayName(request.getDisplayName());
        }
        if (request.getModelType() != null) {
            model.setModelType(request.getModelType());
        }
        if (request.getApiBase() != null) {
            model.setApiBase(request.getApiBase());
        }
        if (request.getApiKey() != null && !request.getApiKey().isEmpty()) {
            model.setApiKey(encryptionUtil.encrypt(request.getApiKey()));
        }
        if (request.getDefaultConfig() != null) {
            model.setDefaultConfig(JsonUtil.toJson(request.getDefaultConfig()));
        }
        if (request.getIsActive() != null) {
            if (request.getIsActive() == 0) {
                List<AiAgent> relatedAgents = agentMapper.selectByModelId(id);
                if (!relatedAgents.isEmpty() && 
                    request.getDisableRelatedAgents() != null && 
                    request.getDisableRelatedAgents()) {
                    List<String> agentIds = relatedAgents.stream()
                            .map(AiAgent::getId)
                            .collect(Collectors.toList());
                    agentMapper.batchUpdateStatus(agentIds, "offline");
                    log.info("禁用模型时，同时禁用了 {} 个关联的智能体: modelId={}, agentIds={}", 
                            agentIds.size(), id, agentIds);
                }
            }
            model.setIsActive(request.getIsActive());
        }
        
        modelMapper.update(model);
        log.info("LLM模型更新成功: id={}", id);
    }
    
    @Override
    public int countRelatedAgents(String id) {
        List<AiAgent> relatedAgents = agentMapper.selectByModelId(id);
        return relatedAgents.size();
    }
    
    @Override
    public void deleteModel(String id) {
        LlmModel model = modelMapper.selectById(id);
        if (model == null) {
            throw new BusinessException(ErrorCode.LLM_MODEL_NOT_FOUND);
        }
        
        modelMapper.deleteById(id);
        log.info("LLM模型删除成功: id={}", id);
    }
    
    @Override
    public String testChat(TestChatRequest request) {
        log.info("开始测试对话: providerId={}, modelName={}", request.getProviderId(), request.getModelName());
        
        try {
            LlmProvider provider = providerMapper.selectById(request.getProviderId());
            if (provider == null) {
                throw new BusinessException(ErrorCode.LLM_PROVIDER_NOT_FOUND);
            }
            
            String apiBase = request.getApiBase() != null && !request.getApiBase().isEmpty()
                    ? request.getApiBase() 
                    : provider.getApiBase();
                    
            if (apiBase == null || apiBase.isEmpty()) {
                throw new BusinessException(ErrorCode.LLM_CALL_ERROR, "API地址未配置");
            }
            
            if (!apiBase.endsWith("/")) {
                apiBase = apiBase + "/";
            }
            
            String apiKey = request.getApiKey() != null && !request.getApiKey().isEmpty()
                    ? request.getApiKey()
                    : (provider.getApiKey() != null ? encryptionUtil.decrypt(provider.getApiKey()) : null);
                    
            if (apiKey == null || apiKey.isEmpty()) {
                throw new BusinessException(ErrorCode.LLM_CALL_ERROR, "API密钥未配置");
            }
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", request.getModelName());
            requestBody.put("messages", List.of(Map.of("role", "user", "content", request.getMessage())));
            requestBody.putAll(request.getConfig());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            String apiUrl = apiBase + "chat/completions";
            log.info("测试调用LLM API: url={}, model={}", apiUrl, request.getModelName());
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );
            
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new BusinessException(ErrorCode.LLM_CALL_ERROR, "API调用失败");
            }
            
            Map<String, Object> body = response.getBody();
            List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
            
            if (choices == null || choices.isEmpty()) {
                throw new BusinessException(ErrorCode.LLM_CALL_ERROR, "API返回空响应");
            }
            
            Map<String, Object> firstChoice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            
            if (message == null) {
                throw new BusinessException(ErrorCode.LLM_CALL_ERROR, "API响应格式错误");
            }
            
            String content = (String) message.get("content");
            if (content == null) {
                content = "API返回内容为空";
            }
            
            log.info("测试对话成功: model={}, response_length={}", request.getModelName(), content.length());
            return content;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("测试对话失败: providerId={}, modelName={}, error={}", 
                    request.getProviderId(), request.getModelName(), e.getMessage(), e);
            throw new BusinessException(ErrorCode.LLM_CALL_ERROR, "测试对话失败: " + e.getMessage());
        }
    }
}

