package com.soulmate.module.sensitive.service.impl;

import com.soulmate.common.BusinessException;
import com.soulmate.common.ErrorCode;
import com.soulmate.common.PageResult;
import com.soulmate.module.sensitive.dto.SensitiveWordRequest;
import com.soulmate.module.sensitive.dto.SensitiveWordVO;
import com.soulmate.module.sensitive.entity.SysSensitiveWord;
import com.soulmate.module.sensitive.mapper.SysSensitiveWordMapper;
import com.soulmate.module.sensitive.service.SensitiveWordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 敏感词服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveWordServiceImpl implements SensitiveWordService {
    
    private final SysSensitiveWordMapper sensitiveWordMapper;
    
    @Override
    public PageResult<SensitiveWordVO> listSensitiveWords(int page, int size, String category, String action, Integer isActive, String keyword) {
        int offset = (page - 1) * size;
        List<SysSensitiveWord> words = sensitiveWordMapper.selectPage(offset, size, category, action, isActive, keyword);
        long total = sensitiveWordMapper.countByCondition(category, action, isActive, keyword);
        
        List<SensitiveWordVO> list = words.stream()
                .map(SensitiveWordVO::fromEntity)
                .collect(Collectors.toList());
        
        return PageResult.of(list, total, page, size);
    }
    
    @Override
    public Long createSensitiveWord(SensitiveWordRequest request) {
        SysSensitiveWord word = new SysSensitiveWord();
        word.setWord(request.getWord());
        word.setCategory(request.getCategory());
        word.setAction(request.getAction());
        word.setReplacement(request.getReplacement());
        word.setIsActive(request.getIsActive());
        
        sensitiveWordMapper.insert(word);
        log.info("敏感词创建成功: id={}, word={}", word.getId(), word.getWord());
        return word.getId();
    }
    
    @Override
    public void updateSensitiveWord(Long id, SensitiveWordRequest request) {
        SysSensitiveWord word = sensitiveWordMapper.selectById(id);
        if (word == null) {
            throw new BusinessException(ErrorCode.SENSITIVE_WORD_NOT_FOUND);
        }
        
        word.setWord(request.getWord());
        word.setCategory(request.getCategory());
        word.setAction(request.getAction());
        word.setReplacement(request.getReplacement());
        word.setIsActive(request.getIsActive());
        
        sensitiveWordMapper.update(word);
        log.info("敏感词更新成功: id={}", id);
    }
    
    @Override
    public void deleteSensitiveWord(Long id) {
        SysSensitiveWord word = sensitiveWordMapper.selectById(id);
        if (word == null) {
            throw new BusinessException(ErrorCode.SENSITIVE_WORD_NOT_FOUND);
        }
        sensitiveWordMapper.deleteById(id);
        log.info("敏感词删除成功: id={}", id);
    }
    
    @Override
    public void updateSensitiveWordStatus(Long id, Integer isActive) {
        SysSensitiveWord word = sensitiveWordMapper.selectById(id);
        if (word == null) {
            throw new BusinessException(ErrorCode.SENSITIVE_WORD_NOT_FOUND);
        }
        word.setIsActive(isActive);
        sensitiveWordMapper.update(word);
        log.info("敏感词状态更新成功: id={}, isActive={}", id, isActive);
    }
    
    @Override
    public List<SensitiveWordVO> getActiveSensitiveWords() {
        return sensitiveWordMapper.selectActiveWords().stream()
                .map(SensitiveWordVO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public void batchUpdateStatus(List<Long> ids, Integer isActive) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        sensitiveWordMapper.batchUpdateStatus(ids, isActive);
        log.info("批量更新敏感词状态成功: ids={}, isActive={}", ids, isActive);
    }
    
    @Override
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        sensitiveWordMapper.batchDelete(ids);
        log.info("批量删除敏感词成功: ids={}", ids);
    }
}
