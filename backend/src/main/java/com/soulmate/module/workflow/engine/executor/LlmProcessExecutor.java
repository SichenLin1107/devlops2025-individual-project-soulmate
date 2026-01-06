package com.soulmate.module.workflow.engine.executor;

import com.soulmate.module.agent.entity.AiAgent;
import com.soulmate.module.agent.mapper.AiAgentMapper;
import com.soulmate.module.chat.entity.ChatMessage;
import com.soulmate.module.chat.mapper.ChatMessageMapper;
import com.soulmate.module.llm.service.LlmClient;
import com.soulmate.module.workflow.dto.NodeOutput;
import com.soulmate.module.workflow.dto.NodesConfig;
import com.soulmate.module.workflow.dto.WorkflowContext;
import com.soulmate.module.workflow.engine.AbstractNodeExecutor;
import com.soulmate.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * LLM处理节点执行器
 * 调用大语言模型生成回复
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LlmProcessExecutor extends AbstractNodeExecutor {
    
    private final LlmClient llmClient;
    private final AiAgentMapper agentMapper;
    private final ChatMessageMapper chatMessageMapper;
    
    // 危机干预提示词
    private static final String CRISIS_INTERVENTION_PROMPT = """
            【重要提示】用户可能处于心理危机状态，请你：
            1. 首先表达对用户情绪的理解和关心
            2. 温和地询问用户的具体情况
            3. 提供情感支持和陪伴
            4. 如有必要，建议用户寻求专业心理援助（如拨打心理援助热线）
            5. 避免任何可能加重用户负面情绪的言论
            
            全国心理援助热线：400-161-9995
            北京心理危机研究与干预中心：010-82951332
            生命热线：400-821-1215
            """;
    
    @Override
    public String getNodeType() {
        return "llm_process";
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected NodeOutput doExecute(NodesConfig.Node node, WorkflowContext context) {
        String userInput = (String) context.getVariable("current_text");
        if (userInput == null) {
            userInput = context.getUserInput();
        }
        
        // 获取节点配置（同时支持驼峰和下划线命名）
        String modelId = getConfig(node, "modelId", null);
        String customPrompt = getConfig(node, "systemPrompt", null);
        Double temperature = getConfig(node, "temperature", 0.7);
        Integer maxTokens = getConfig(node, "maxTokens", 2000);
        Integer historyCount = getConfig(node, "contextWindow", 10);
        
        // 获取智能体信息
        String agentId = context.getAgentId();
        AiAgent agent = null;
        if (agentId != null) {
            agent = agentMapper.selectById(agentId);
        }
        
        // 确定使用的模型
        if (modelId == null && agent != null) {
            modelId = agent.getModelId();
        }
        
        if (modelId == null) {
            return failedOutput("未配置LLM模型");
        }
        
        // 构建消息列表
        List<com.soulmate.module.llm.dto.ChatMessage> messages = new ArrayList<>();
        
        // 1. 系统提示词
        String systemPrompt = buildSystemPrompt(agent, customPrompt, context);
        messages.add(new com.soulmate.module.llm.dto.ChatMessage("system", systemPrompt));
        
        // 2. 历史消息
        if (context.getSessionId() != null && historyCount > 0) {
            List<ChatMessage> historyMessages = chatMessageMapper.selectRecentMessages(
                    context.getSessionId(), historyCount);
            for (ChatMessage msg : historyMessages) {
                if (!"system".equals(msg.getRole())) {
                    messages.add(new com.soulmate.module.llm.dto.ChatMessage(msg.getRole(), msg.getContent()));
                }
            }
        }
        
        // 3. 当前用户输入
        messages.add(new com.soulmate.module.llm.dto.ChatMessage("user", userInput));
        
        // 构建模型配置
        Map<String, Object> config = new HashMap<>();
        config.put("temperature", temperature);
        config.put("max_tokens", maxTokens);
        
        // 如果有智能体配置，合并
        if (agent != null && agent.getModelConfig() != null) {
            Map<String, Object> agentConfig = JsonUtil.parse(agent.getModelConfig(), Map.class);
            if (agentConfig != null) {
                // 节点配置优先
                agentConfig.putAll(config);
                config = agentConfig;
            }
        }
        
        // 调用LLM
        long startTime = System.currentTimeMillis();
        String response;
        try {
            response = llmClient.chat(modelId, messages, config);
        } catch (Exception e) {
            log.error("LLM调用失败: modelId={}, error={}", modelId, e.getMessage(), e);
            return failedOutput("AI服务暂时不可用：" + e.getMessage());
        }
        long duration = System.currentTimeMillis() - startTime;
        
        // 更新上下文
        context.setFinalResponse(response);
        context.setVariable("llm_response", response);
        
        Map<String, Object> data = new HashMap<>();
        data.put("response", response);
        data.put("model_id", modelId);
        data.put("duration_ms", duration);
        data.put("message_count", messages.size());
        
        log.info("LLM处理完成: modelId={}, duration={}ms, responseLength={}", 
                modelId, duration, response.length());
        
        return successOutput(data);
    }
    
    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(AiAgent agent, String customPrompt, WorkflowContext context) {
        StringBuilder promptBuilder = new StringBuilder();
        
        // 使用自定义提示词或智能体提示词
        if (customPrompt != null && !customPrompt.isEmpty()) {
            promptBuilder.append(customPrompt);
        } else if (agent != null && agent.getSystemPrompt() != null) {
            promptBuilder.append(agent.getSystemPrompt());
        } else {
            promptBuilder.append("你是一个友善的心理陪伴助手，请用温暖、理解的态度与用户交流。");
        }
        
        // 添加知识上下文
        String knowledgeContext = (String) context.getVariable("knowledge_context");
        if (knowledgeContext != null && !knowledgeContext.isEmpty()) {
            promptBuilder.append("\n\n").append(knowledgeContext);
            promptBuilder.append("\n请基于以上参考知识回答用户的问题，如果参考知识中没有相关信息，则基于你的知识回答。");
        }
        
        // 危机干预
        if (context.isCrisis()) {
            promptBuilder.append("\n\n").append(CRISIS_INTERVENTION_PROMPT);
        }
        
        // 添加情绪提示
        String emotion = context.getEmotion();
        if (emotion != null && !"neutral".equals(emotion)) {
            if ("negative".equals(emotion)) {
                promptBuilder.append("\n\n【情绪提示】用户当前情绪偏向负面，请给予更多的关心和理解。");
            } else if ("positive".equals(emotion)) {
                promptBuilder.append("\n\n【情绪提示】用户当前情绪较为积极，可以给予鼓励和肯定。");
            }
        }
        
        return promptBuilder.toString();
    }
}

