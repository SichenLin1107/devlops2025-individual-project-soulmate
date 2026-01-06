package com.soulmate.module.workflow.engine.executor;

import com.soulmate.module.workflow.dto.NodeOutput;
import com.soulmate.module.workflow.dto.NodesConfig;
import com.soulmate.module.workflow.dto.WorkflowContext;
import com.soulmate.module.workflow.engine.AbstractNodeExecutor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 文本处理节点执行器
 * 负责文本预处理：清洗、规范化、情感分析等
 */
@Component
public class TextProcessExecutor extends AbstractNodeExecutor {
    
    @Override
    public String getNodeType() {
        return "text_process";
    }
    
    @Override
    protected NodeOutput doExecute(NodesConfig.Node node, WorkflowContext context) {
        // 获取当前处理文本
        String inputText = (String) context.getVariable("current_text");
        if (inputText == null) {
            inputText = context.getUserInput();
        }
        
        // 获取节点配置
        Boolean enableTrim = getConfig(node, "enable_trim", true);
        Boolean enableNormalize = getConfig(node, "enable_normalize", true);
        Integer maxLength = getConfig(node, "max_length", 2000);
        
        String processedText = inputText;
        
        // 1. 去除首尾空白
        if (enableTrim) {
            processedText = processedText.trim();
        }
        
        // 2. 规范化处理（去除多余空白、统一换行符等）
        if (enableNormalize) {
            processedText = normalizeText(processedText);
        }
        
        // 3. 长度限制
        if (processedText.length() > maxLength) {
            processedText = processedText.substring(0, maxLength);
        }
        
        // 4. 简单的情绪关键词检测
        String detectedEmotion = detectEmotion(processedText);
        
        // 更新上下文
        context.setVariable("current_text", processedText);
        context.setVariable("original_text", inputText);
        if (detectedEmotion != null) {
            context.setEmotion(detectedEmotion);
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("input_text", inputText);
        data.put("processed_text", processedText);
        data.put("text_length", processedText.length());
        data.put("detected_emotion", detectedEmotion);
        
        return successOutput(data);
    }
    
    /**
     * 规范化文本
     */
    private String normalizeText(String text) {
        // 统一换行符
        text = text.replaceAll("\r\n", "\n").replaceAll("\r", "\n");
        // 去除多余空行
        text = text.replaceAll("\n{3,}", "\n\n");
        // 去除多余空格
        text = text.replaceAll(" {2,}", " ");
        return text;
    }
    
    /**
     * 简单的情绪检测（基于关键词）
     */
    private String detectEmotion(String text) {
        String lowerText = text.toLowerCase();
        
        // 负面情绪关键词
        if (containsAny(lowerText, "难过", "伤心", "痛苦", "绝望", "抑郁", "焦虑", "害怕", "恐惧", "愤怒", "生气")) {
            return "negative";
        }
        
        // 正面情绪关键词
        if (containsAny(lowerText, "开心", "高兴", "快乐", "幸福", "感谢", "期待", "兴奋", "满足")) {
            return "positive";
        }
        
        return "neutral";
    }
    
    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}

