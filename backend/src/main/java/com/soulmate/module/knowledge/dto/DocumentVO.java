package com.soulmate.module.knowledge.dto;

import com.soulmate.module.knowledge.entity.AiKnowledgeDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文档视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentVO {
    
    private Long id;
    private String kbId;
    private String fileName;
    private String fileType;
    private Integer fileSize;
    private Integer segmentCount;
    private String status;
    private LocalDateTime createdAt;
    
    public static DocumentVO fromEntity(AiKnowledgeDocument entity) {
        if (entity == null) {
            return null;
        }
        return DocumentVO.builder()
                .id(entity.getId())
                .kbId(entity.getKbId())
                .fileName(entity.getFileName())
                .fileType(entity.getFileType())
                .fileSize(entity.getFileSize())
                .segmentCount(entity.getSegmentCount())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
