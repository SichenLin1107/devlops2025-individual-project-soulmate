package com.soulmate.module.llm.service.impl;

import com.soulmate.common.BusinessException;
import com.soulmate.common.ErrorCode;
import com.soulmate.module.llm.dto.LlmProviderRequest;
import com.soulmate.module.llm.dto.LlmProviderVO;
import com.soulmate.module.llm.entity.LlmProvider;
import com.soulmate.module.llm.mapper.LlmModelMapper;
import com.soulmate.module.llm.mapper.LlmProviderMapper;
import com.soulmate.module.llm.service.LlmProviderService;
import com.soulmate.util.EncryptionUtil;
import com.soulmate.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * LLM提供商服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmProviderServiceImpl implements LlmProviderService {
    
    private final LlmProviderMapper providerMapper;
    private final LlmModelMapper modelMapper;
    private final EncryptionUtil encryptionUtil;
    
    @Override
    public List<LlmProviderVO> listProviders() {
        return providerMapper.selectAll().stream()
                .map(provider -> {
                    String decryptedApiKey = provider.getApiKey() != null ? 
                            encryptionUtil.decrypt(provider.getApiKey()) : null;
                    return LlmProviderVO.fromEntity(provider, decryptedApiKey);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public String createProvider(LlmProviderRequest request) {
        LlmProvider provider = new LlmProvider();
        provider.setId(IdGenerator.providerId());
        provider.setName(request.getName());
        provider.setApiBase(request.getApiBase());
        
        if (request.getApiKey() != null && !request.getApiKey().isEmpty()) {
            provider.setApiKey(encryptionUtil.encrypt(request.getApiKey()));
        }
        
        provider.setIsActive(request.getIsActive() != null ? request.getIsActive() : 1);
        
        providerMapper.insert(provider);
        log.info("LLM提供商创建成功: id={}, name={}", provider.getId(), provider.getName());
        return provider.getId();
    }
    
    @Override
    public void updateProvider(String id, LlmProviderRequest request) {
        LlmProvider provider = providerMapper.selectById(id);
        if (provider == null) {
            throw new BusinessException(ErrorCode.LLM_PROVIDER_NOT_FOUND);
        }
        
        if (request.getName() != null) {
            provider.setName(request.getName());
        }
        if (request.getApiBase() != null) {
            provider.setApiBase(request.getApiBase());
        }
        if (request.getApiKey() != null && !request.getApiKey().isEmpty()) {
            provider.setApiKey(encryptionUtil.encrypt(request.getApiKey()));
        }
        if (request.getIsActive() != null) {
            provider.setIsActive(request.getIsActive());
        }
        
        providerMapper.update(provider);
        log.info("LLM提供商更新成功: id={}", id);
    }
    
    @Override
    public void deleteProvider(String id) {
        LlmProvider provider = providerMapper.selectById(id);
        if (provider == null) {
            throw new BusinessException(ErrorCode.LLM_PROVIDER_NOT_FOUND);
        }
        
        if (!modelMapper.selectByProviderId(id).isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请先删除该提供商下的所有模型");
        }
        
        providerMapper.deleteById(id);
        log.info("LLM提供商删除成功: id={}", id);
    }
}

