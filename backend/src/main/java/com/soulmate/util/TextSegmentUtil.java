package com.soulmate.util;

import lombok.extern.slf4j.Slf4j;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 文本切片工具类，提供文本分段功能，支持句子边界检测和重叠切片
 */
@Slf4j
public class TextSegmentUtil {
    
    public static final int DEFAULT_CHUNK_SIZE = 200;
    public static final int DEFAULT_CHUNK_OVERLAP = 50;
    public static final int MIN_CHUNK_SIZE = 100;
    public static final double MAX_OVERLAP_RATIO = 0.5;
    
    /**
     * 使用默认参数切分文本
     */
    public static List<String> segmentText(String text) {
        return segmentText(text, DEFAULT_CHUNK_SIZE, DEFAULT_CHUNK_OVERLAP);
    }
    
    /**
     * 切分文本为片段
     */
    public static List<String> segmentText(String text, int chunkSize, int overlap) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        text = text.trim();
        chunkSize = Math.max(chunkSize, MIN_CHUNK_SIZE);
        overlap = Math.max(0, Math.min(overlap, (int) (chunkSize * MAX_OVERLAP_RATIO)));
        
        if (text.length() <= chunkSize) {
            List<String> result = new ArrayList<>();
            result.add(text);
            return result;
        }
        
        List<String> sentences = splitIntoSentences(text);
        return createChunksFromSentences(sentences, chunkSize, overlap);
    }
    
    /**
     * 将文本按句子分割
     */
    private static List<String> splitIntoSentences(String text) {
        List<String> sentences = new ArrayList<>();
        BreakIterator sentenceIterator = BreakIterator.getSentenceInstance(Locale.CHINESE);
        sentenceIterator.setText(text);
        
        int start = sentenceIterator.first();
        int end = sentenceIterator.next();
        
        while (end != BreakIterator.DONE) {
            String sentence = text.substring(start, end).trim();
            if (!sentence.isEmpty()) {
                sentences.add(sentence);
            }
            start = end;
            end = sentenceIterator.next();
        }
        
        if (sentences.isEmpty()) {
            sentences = splitByPunctuation(text);
        }
        
        return sentences;
    }
    
    /**
     * 按标点符号简单分割文本
     */
    private static List<String> splitByPunctuation(String text) {
        List<String> sentences = new ArrayList<>();
        String[] parts = text.split("[。！？.!?]");
        
        for (String part : parts) {
            part = part.trim();
            if (!part.isEmpty()) {
                sentences.add(part);
            }
        }
        
        if (sentences.isEmpty()) {
            int maxSentenceLength = 200;
            for (int i = 0; i < text.length(); i += maxSentenceLength) {
                int end = Math.min(i + maxSentenceLength, text.length());
                sentences.add(text.substring(i, end));
            }
        }
        
        return sentences;
    }
    
    /**
     * 根据句子创建文本切片
     */
    private static List<String> createChunksFromSentences(List<String> sentences, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        
        if (sentences.isEmpty()) {
            return chunks;
        }
        
        StringBuilder currentChunk = new StringBuilder();
        int currentLength = 0;
        int startIndex = 0;
        
        for (int i = 0; i < sentences.size(); i++) {
            String sentence = sentences.get(i);
            
            if (currentLength > 0 && currentLength + sentence.length() > chunkSize) {
                chunks.add(currentChunk.toString().trim());
                
                int overlapStartIndex = findOverlapStartIndex(sentences, startIndex, i, overlap);
                currentChunk = new StringBuilder();
                currentLength = 0;
                
                for (int j = overlapStartIndex; j < i; j++) {
                    int sentenceLength = sentences.get(j).length();
                    if (currentLength + sentenceLength <= overlap) {
                        if (currentChunk.length() > 0) {
                            currentChunk.append(" ");
                            currentLength++;
                        }
                        currentChunk.append(sentences.get(j));
                        currentLength += sentenceLength;
                    } else {
                        break;
                    }
                }
                
                startIndex = overlapStartIndex;
            }
            
            if (currentChunk.length() > 0) {
                currentChunk.append(" ");
                currentLength++;
            }
            currentChunk.append(sentence);
            currentLength += sentence.length();
        }
        
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }
        
        return chunks;
    }
    
    /**
     * 找到重叠部分的开始索引
     */
    private static int findOverlapStartIndex(List<String> sentences, int chunkStartIndex, int chunkEndIndex, int overlap) {
        if (overlap == 0) {
            return chunkEndIndex;
        }
        
        int totalLength = 0;
        int startIndex = chunkEndIndex - 1;
        
        for (int i = chunkEndIndex - 1; i >= chunkStartIndex; i--) {
            totalLength += sentences.get(i).length();
            if (totalLength >= overlap) {
                startIndex = i;
                break;
            }
            startIndex = i;
        }
        
        return startIndex;
    }
    
    /**
     * 预览文本切片结果（用于调试）
     */
    public static void previewSegments(String text, int chunkSize, int overlap) {
        List<String> segments = segmentText(text, chunkSize, overlap);
        
        System.out.println("=== 文本切片预览 ===");
        System.out.println("原文长度: " + text.length());
        System.out.println("切片参数: chunkSize=" + chunkSize + ", overlap=" + overlap);
        System.out.println("切片数量: " + segments.size());
        System.out.println();
        
        for (int i = 0; i < segments.size(); i++) {
            String segment = segments.get(i);
            System.out.println("片段 " + (i + 1) + " (长度: " + segment.length() + "):");
            System.out.println(segment);
            System.out.println("---");
        }
    }
    
    /**
     * 计算文本切片的统计信息
     */
    public static SegmentStats calculateStats(String text, int chunkSize, int overlap) {
        List<String> segments = segmentText(text, chunkSize, overlap);
        
        int totalSegments = segments.size();
        int totalLength = text.length();
        int avgSegmentLength = segments.stream().mapToInt(String::length).sum() / Math.max(1, totalSegments);
        int minSegmentLength = segments.stream().mapToInt(String::length).min().orElse(0);
        int maxSegmentLength = segments.stream().mapToInt(String::length).max().orElse(0);
        
        return new SegmentStats(totalSegments, totalLength, avgSegmentLength, minSegmentLength, maxSegmentLength);
    }
    
    /**
     * 切片统计信息
     */
    public static class SegmentStats {
        private final int totalSegments;
        private final int totalLength;
        private final int avgSegmentLength;
        private final int minSegmentLength;
        private final int maxSegmentLength;
        
        public SegmentStats(int totalSegments, int totalLength, int avgSegmentLength, 
                          int minSegmentLength, int maxSegmentLength) {
            this.totalSegments = totalSegments;
            this.totalLength = totalLength;
            this.avgSegmentLength = avgSegmentLength;
            this.minSegmentLength = minSegmentLength;
            this.maxSegmentLength = maxSegmentLength;
        }
        
        public int getTotalSegments() { return totalSegments; }
        public int getTotalLength() { return totalLength; }
        public int getAvgSegmentLength() { return avgSegmentLength; }
        public int getMinSegmentLength() { return minSegmentLength; }
        public int getMaxSegmentLength() { return maxSegmentLength; }
        
        @Override
        public String toString() {
            return String.format(
                "SegmentStats{totalSegments=%d, totalLength=%d, avgLength=%d, minLength=%d, maxLength=%d}",
                totalSegments, totalLength, avgSegmentLength, minSegmentLength, maxSegmentLength
            );
        }
    }
}
