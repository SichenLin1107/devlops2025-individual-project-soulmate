package com.soulmate.module.knowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * RAG检索结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetrievalResult {
    
    private String text;
    private Double score;
    private Long docId;
    private String segmentId;
    private Map<String, Object> metadata;
}

