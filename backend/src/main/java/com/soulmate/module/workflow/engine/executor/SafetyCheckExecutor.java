package com.soulmate.module.workflow.engine.executor;

import com.soulmate.module.sensitive.dto.SensitiveWordVO;
import com.soulmate.module.sensitive.service.SensitiveWordService;
import com.soulmate.module.workflow.dto.NodeOutput;
import com.soulmate.module.workflow.dto.NodesConfig;
import com.soulmate.module.workflow.dto.WorkflowContext;
import com.soulmate.module.workflow.engine.AbstractNodeExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 安全检测节点执行器
 * 基于敏感词表进行文本安全检测
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SafetyCheckExecutor extends AbstractNodeExecutor {
    
    private final SensitiveWordService sensitiveWordService;
    
    // 危机关键词（用于检测严重心理危机）
    private static final Set<String> CRISIS_KEYWORDS = Set.of(
            "自杀", "自残", "不想活", "想死", "结束生命", "跳楼", "割腕", "服药自杀",
            "活着没意思", "活不下去", "去死", "寻死"
    );
    
    @Override
    public String getNodeType() {
        return "safety_check";
    }
    
    @Override
    protected NodeOutput doExecute(NodesConfig.Node node, WorkflowContext context) {
        String inputText = (String) context.getVariable("current_text");
        if (inputText == null) {
            inputText = context.getUserInput();
        }
        
        // 获取所有启用的敏感词
        List<SensitiveWordVO> sensitiveWords = sensitiveWordService.getActiveSensitiveWords();
        
        // 检测结果
        List<Map<String, Object>> detectedWords = new ArrayList<>();
        boolean isSafe = true;
        boolean isCrisis = false;
        String suggestedAction = "pass";
        String processedText = inputText;
        
        // 1. 检测危机关键词
        for (String keyword : CRISIS_KEYWORDS) {
            if (inputText.contains(keyword)) {
                isCrisis = true;
                Map<String, Object> detection = new HashMap<>();
                detection.put("word", keyword);
                detection.put("category", "crisis");
                detection.put("action", "crisis_intervention");
                detectedWords.add(detection);
            }
        }
        
        // 2. 检测敏感词
        for (SensitiveWordVO word : sensitiveWords) {
            if (inputText.contains(word.getWord())) {
                isSafe = false;
                
                Map<String, Object> detection = new HashMap<>();
                detection.put("word", word.getWord());
                detection.put("category", word.getCategory());
                detection.put("action", word.getAction());
                detectedWords.add(detection);
                
                // 根据动作类型处理
                String action = word.getAction();
                if ("block".equals(action)) {
                    suggestedAction = "block";
                } else if ("replace".equals(action) && !"block".equals(suggestedAction)) {
                    suggestedAction = "replace";
                    // 替换敏感词
                    String replacement = word.getReplacement() != null ? word.getReplacement() : "***";
                    processedText = processedText.replace(word.getWord(), replacement);
                } else if ("warn".equals(action) && "pass".equals(suggestedAction)) {
                    suggestedAction = "warn";
                }
            }
        }
        
        // 更新上下文
        if (isCrisis) {
            context.setCrisis(true);
            suggestedAction = "crisis_intervention";
        }
        
        // 如果执行替换，更新当前文本
        if ("replace".equals(suggestedAction)) {
            context.setVariable("current_text", processedText);
        }
        
        // 设置安全检测变量供后续节点使用
        context.setVariable("safety_passed", isSafe && !isCrisis);
        context.setVariable("is_crisis", isCrisis);
        context.setVariable("safety_action", suggestedAction);
        
        Map<String, Object> data = new HashMap<>();
        data.put("is_safe", isSafe);
        data.put("is_crisis", isCrisis);
        data.put("detected_words", detectedWords);
        data.put("detected_count", detectedWords.size());
        data.put("suggested_action", suggestedAction);
        data.put("processed_text", processedText);
        data.put("categories", detectedWords.stream()
                .map(d -> (String) d.get("category"))
                .distinct()
                .collect(Collectors.toList()));
        
        log.info("安全检测完成: isSafe={}, isCrisis={}, detectedCount={}, action={}", 
                isSafe, isCrisis, detectedWords.size(), suggestedAction);
        
        return successOutput(data);
    }
}

