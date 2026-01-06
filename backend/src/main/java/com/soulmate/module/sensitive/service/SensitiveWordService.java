package com.soulmate.module.sensitive.service;

import com.soulmate.common.PageResult;
import com.soulmate.module.sensitive.dto.SensitiveWordRequest;
import com.soulmate.module.sensitive.dto.SensitiveWordVO;

import java.util.List;

/**
 * 敏感词服务接口
 */
public interface SensitiveWordService {
    
    /**
     * 获取敏感词列表
     */
    PageResult<SensitiveWordVO> listSensitiveWords(int page, int size, String category, String action, Integer isActive, String keyword);
    
    /**
     * 创建敏感词
     */
    Long createSensitiveWord(SensitiveWordRequest request);
    
    /**
     * 更新敏感词
     */
    void updateSensitiveWord(Long id, SensitiveWordRequest request);
    
    /**
     * 删除敏感词
     */
    void deleteSensitiveWord(Long id);
    
    /**
     * 更新敏感词状态
     */
    void updateSensitiveWordStatus(Long id, Integer isActive);
    
    /**
     * 获取所有启用的敏感词
     */
    List<SensitiveWordVO> getActiveSensitiveWords();
    
    /**
     * 批量更新敏感词状态
     */
    void batchUpdateStatus(List<Long> ids, Integer isActive);
    
    /**
     * 批量删除敏感词
     */
    void batchDelete(List<Long> ids);
}
