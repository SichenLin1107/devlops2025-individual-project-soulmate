package com.soulmate.module.sensitive.dto;

import com.soulmate.module.sensitive.entity.SysSensitiveWord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 敏感词VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveWordVO {
    
    private Long id;
    private String word;
    private String category;
    private String action;
    private String replacement;
    private Integer isActive;
    private LocalDateTime createdAt;
    
    public static SensitiveWordVO fromEntity(SysSensitiveWord entity) {
        if (entity == null) {
            return null;
        }
        return SensitiveWordVO.builder()
                .id(entity.getId())
                .word(entity.getWord())
                .category(entity.getCategory())
                .action(entity.getAction())
                .replacement(entity.getReplacement())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
