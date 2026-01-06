package com.soulmate.module.chat.service.impl;

import com.soulmate.common.BusinessException;
import com.soulmate.common.ErrorCode;
import com.soulmate.common.PageResult;
import com.soulmate.module.agent.entity.AiAgent;
import com.soulmate.module.agent.entity.AiAgentKnowledge;
import com.soulmate.module.agent.mapper.AiAgentKnowledgeMapper;
import com.soulmate.module.agent.mapper.AiAgentMapper;
import com.soulmate.module.chat.dto.*;
import com.soulmate.module.chat.entity.ChatMessage;
import com.soulmate.module.chat.entity.ChatSession;
import com.soulmate.module.chat.mapper.ChatMessageMapper;
import com.soulmate.module.chat.mapper.ChatSessionMapper;
import com.soulmate.module.chat.service.MessageService;
import com.soulmate.module.knowledge.dto.RetrievalResult;
import com.soulmate.module.knowledge.entity.AiKnowledgeBase;
import com.soulmate.module.knowledge.mapper.AiKnowledgeBaseMapper;
import com.soulmate.module.knowledge.service.RagClient;
import com.soulmate.module.llm.service.LlmClient;
import com.soulmate.module.workflow.dto.NodesConfig;
import com.soulmate.module.workflow.dto.WorkflowContext;
import com.soulmate.module.workflow.dto.WorkflowResult;
import com.soulmate.module.workflow.engine.WorkflowEngine;
import com.soulmate.module.workflow.entity.AiWorkflow;
import com.soulmate.module.workflow.mapper.AiWorkflowMapper;
import com.soulmate.security.UserContext;
import com.soulmate.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 消息服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final AiAgentMapper agentMapper;
    private final AiAgentKnowledgeMapper agentKnowledgeMapper;
    private final AiKnowledgeBaseMapper knowledgeBaseMapper;
    private final AiWorkflowMapper workflowMapper;
    private final LlmClient llmClient;
    private final RagClient ragClient;
    private final WorkflowEngine workflowEngine;
    
    @Override
    public PageResult<MessageVO> listMessages(Long sessionId, int page, int size) {
        ChatSession session = validateAndGetSession(sessionId);
        
        int offset = (page - 1) * size;
        List<ChatMessage> messages = messageMapper.selectPage(sessionId, offset, size);
        long total = messageMapper.countBySessionId(sessionId);
        
        List<MessageVO> list = messages.stream()
                .map(MessageVO::fromEntity)
                .collect(Collectors.toList());
        
        return PageResult.of(list, total, page, size);
    }
    
    @Override
    @Transactional
    public SendMessageResponse sendMessage(Long sessionId, SendMessageRequest request) {
        ChatSession session = validateAndGetSession(sessionId);
        
        // 获取智能体
        AiAgent agent = agentMapper.selectById(session.getAgentId());
        if (agent == null) {
            throw new BusinessException(ErrorCode.AGENT_NOT_FOUND);
        }
        
        // 保存用户消息
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(request.getContent());
        userMsg.setMsgType("text");
        messageMapper.insert(userMsg);
        
        // 生成AI回复
        String responseContent;
        List<String> referencedKbNames = new ArrayList<>();
        
        // ========== 核心改动：无论是否有工作流，都先检索智能体绑定的知识库 ==========
        // 智能体绑定的知识库是智能体的基础知识支撑，始终生效
        String agentKnowledgeContext = retrieveKnowledge(agent, request.getContent(), referencedKbNames);
        log.info("【智能体知识库】检索完成: 知识库引用={}, 有知识上下文={}", 
                referencedKbNames, agentKnowledgeContext != null);
        
        // 获取历史消息（两种路径都需要）
        List<ChatMessage> historyMessages = messageMapper.selectRecentMessages(sessionId, 10);
        
        // 检查智能体是否配置了工作流
        if (agent.getWorkflowId() != null && !agent.getWorkflowId().isEmpty()) {
            log.info("【工作流路径】使用工作流处理: workflowId={}", agent.getWorkflowId());
            // 使用工作流引擎处理，传入智能体的知识上下文
            WorkflowResult result = executeWorkflow(agent, sessionId, request.getContent(), 
                    historyMessages, agentKnowledgeContext);
            responseContent = result.getResponse();
            
            // 从工作流元数据中获取额外的知识库引用（如果工作流中有RAG节点）
            if (result.getMetadata() != null && result.getMetadata().get("referenced_kbs") != null) {
                @SuppressWarnings("unchecked")
                List<String> workflowKbs = (List<String>) result.getMetadata().get("referenced_kbs");
                // 合并工作流中RAG节点检索到的知识库（去重）
                for (String kb : workflowKbs) {
                    if (!referencedKbNames.contains(kb)) {
                        referencedKbNames.add(kb);
                    }
                }
                log.info("【工作流路径】合并知识库引用: 最终引用={}", referencedKbNames);
            }
        } else {
            log.info("【直接调用路径】智能体未配置工作流，使用直接LLM调用");
            // 使用直接LLM调用
            responseContent = directLlmCall(agent, request.getContent(), historyMessages, agentKnowledgeContext);
            log.info("【直接调用路径】完成，知识库引用={}", referencedKbNames);
        }
        
        // 保存助手消息
        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setSessionId(sessionId);
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(responseContent);
        assistantMsg.setMsgType("text");
        messageMapper.insert(assistantMsg);
        
        // 更新会话消息数和更新时间
        int newMessageCount = session.getMessageCount() + 2;
        sessionMapper.updateMessageCount(sessionId, newMessageCount);
        
        // 首次对话自动生成标题（开场白1条 + 用户1条 + 助手1条 = 3条）
        String sessionTitle = session.getTitle();
        if (newMessageCount == 3 && "新对话".equals(session.getTitle())) {
            sessionTitle = generateTitle(request.getContent());
            sessionMapper.updateTitle(sessionId, sessionTitle);
        }
        
        // 更新智能体热度
        agentMapper.incrementHeatValue(agent.getId());
        
        log.info("消息发送成功: sessionId={}, userMsgId={}, assistantMsgId={}, usedWorkflow={}, kbRefs={}", 
                sessionId, userMsg.getId(), assistantMsg.getId(), 
                agent.getWorkflowId() != null, referencedKbNames);
        
        List<String> kbRefsToReturn = referencedKbNames.isEmpty() ? null : referencedKbNames;
        log.debug("返回知识库引用: {}", kbRefsToReturn);
        
        return SendMessageResponse.builder()
                .userMessage(MessageVO.fromEntity(userMsg))
                .assistantMessage(MessageVO.fromEntity(assistantMsg))
                .sessionTitle(sessionTitle)
                .referencedKnowledgeBases(kbRefsToReturn)
                .build();
    }
    
    /**
     * 执行工作流
     * @param agent 智能体
     * @param sessionId 会话ID
     * @param userInput 用户输入
     * @param historyMessages 历史消息（已预先获取）
     * @param agentKnowledgeContext 智能体知识库检索结果（已预先检索）
     */
    private WorkflowResult executeWorkflow(AiAgent agent, Long sessionId, String userInput,
                                           List<ChatMessage> historyMessages, String agentKnowledgeContext) {
        // 获取工作流配置
        AiWorkflow workflow = workflowMapper.selectById(agent.getWorkflowId());
        if (workflow == null || workflow.getIsActive() != 1) {
            log.warn("工作流不存在或未启用，回退到直接LLM调用: workflowId={}", agent.getWorkflowId());
            // 回退到直接LLM调用（使用已检索的知识上下文）
            String response = directLlmCall(agent, userInput, historyMessages, agentKnowledgeContext);
            log.info("工作流回退-直接调用完成");
            return WorkflowResult.builder()
                    .response(response)
                    .build();
        }
        
        // 解析节点配置
        NodesConfig nodesConfig = JsonUtil.parse(workflow.getNodesConfig(), NodesConfig.class);
        if (nodesConfig == null || nodesConfig.getNodes() == null || nodesConfig.getNodes().isEmpty()) {
            log.warn("工作流配置无效，回退到直接LLM调用: workflowId={}", workflow.getId());
            String response = directLlmCall(agent, userInput, historyMessages, agentKnowledgeContext);
            log.info("工作流配置无效-直接调用完成");
            return WorkflowResult.builder()
                    .response(response)
                    .build();
        }
        
        // 获取智能体关联的知识库ID（供工作流中的RAG节点额外使用）
        List<String> agentKbIds = getAgentKnowledgeBaseIds(agent.getId());
        
        // 构建工作流上下文
        WorkflowContext context = WorkflowContext.builder()
                .userInput(userInput)
                .sessionId(sessionId)
                .agentId(agent.getId())
                .build();
        
        // ========== 核心改动：将智能体的知识上下文传入工作流 ==========
        // 这样无论工作流中是否有RAG节点，LLM节点都能使用智能体的知识库
        if (agentKnowledgeContext != null && !agentKnowledgeContext.isEmpty()) {
            context.setVariable("agent_knowledge_context", agentKnowledgeContext);
            context.setVariable("knowledge_context", agentKnowledgeContext);  // 设置为当前知识上下文
            context.setVariable("has_knowledge", true);
            log.info("【工作流】已注入智能体知识上下文到工作流");
        }
        
        // 设置智能体知识库ID供工作流中的RAG节点额外使用（可选）
        context.setVariable("agent_kb_ids", agentKbIds);
        
        // 执行工作流
        log.info("开始执行工作流: workflowId={}, agentId={}, hasAgentKnowledge={}", 
                workflow.getId(), agent.getId(), agentKnowledgeContext != null);
        return workflowEngine.execute(nodesConfig, context);
    }
    
    /**
     * 获取智能体关联的知识库ID列表
     */
    private List<String> getAgentKnowledgeBaseIds(String agentId) {
        List<AiAgentKnowledge> agentKnowledgeList = agentKnowledgeMapper.selectByAgentId(agentId);
        if (agentKnowledgeList == null || agentKnowledgeList.isEmpty()) {
            return new ArrayList<>();
        }
        return agentKnowledgeList.stream()
                .map(AiAgentKnowledge::getKbId)
                .collect(Collectors.toList());
    }
    
    /**
     * 检索知识库内容
     */
    private String retrieveKnowledge(AiAgent agent, String userInput, List<String> referencedKbNames) {
        List<AiAgentKnowledge> agentKnowledgeList = agentKnowledgeMapper.selectByAgentId(agent.getId());
        
        if (agentKnowledgeList == null || agentKnowledgeList.isEmpty()) {
            log.debug("智能体未绑定知识库: agentId={}", agent.getId());
            return null;
        }
        
        log.info("开始检索知识库: agentId={}, kbCount={}", agent.getId(), agentKnowledgeList.size());
        
        try {
            List<RetrievalResult> allResults = new ArrayList<>();
            Set<String> kbNameSet = new HashSet<>();
            
            for (AiAgentKnowledge ak : agentKnowledgeList) {
                AiKnowledgeBase kb = knowledgeBaseMapper.selectById(ak.getKbId());
                if (kb != null) {
                    kbNameSet.add(kb.getName());
                    try {
                        List<RetrievalResult> results = ragClient.search(ak.getKbId(), userInput, 3);
                        if (results != null && !results.isEmpty()) {
                            allResults.addAll(results);
                            log.debug("知识库检索成功: kbId={}, kbName={}, count={}", ak.getKbId(), kb.getName(), results.size());
                        }
                    } catch (Exception e) {
                        log.warn("知识库检索失败，跳过: kbId={}, kbName={}, error={}", ak.getKbId(), kb.getName(), e.getMessage());
                    }
                }
            }
            
            if (!allResults.isEmpty()) {
                allResults.sort((a, b) -> Double.compare(
                    b.getScore() != null ? b.getScore() : 0.0,
                    a.getScore() != null ? a.getScore() : 0.0
                ));
                List<RetrievalResult> topResults = allResults.stream()
                        .limit(5)
                        .collect(Collectors.toList());
                
                StringBuilder contextBuilder = new StringBuilder();
                contextBuilder.append("【相关参考知识】\n");
                for (int i = 0; i < topResults.size(); i++) {
                    RetrievalResult result = topResults.get(i);
                    contextBuilder.append(String.format("%d. %s\n", i + 1, result.getText()));
                }
                
                // 只有检索到结果时才添加知识库名称
                referencedKbNames.addAll(kbNameSet);
                log.info("知识库检索完成: agentId={}, kbCount={}, resultCount={}, kbNames={}", 
                        agent.getId(), kbNameSet.size(), topResults.size(), kbNameSet);
                log.debug("检索结果详情: {}", topResults.stream()
                        .map(r -> String.format("score=%.3f, text=%s...", 
                                r.getScore() != null ? r.getScore() : 0.0,
                                r.getText() != null && r.getText().length() > 50 
                                        ? r.getText().substring(0, 50) : r.getText()))
                        .collect(Collectors.joining("; ")));
                
                return contextBuilder.toString();
            } else {
                log.debug("知识库检索无结果: agentId={}, 尝试检索的知识库={}", agent.getId(), kbNameSet);
            }
        } catch (Exception e) {
            log.warn("知识库检索异常: agentId={}, error={}", agent.getId(), e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 验证会话存在性和访问权限
     */
    private ChatSession validateAndGetSession(Long sessionId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ErrorCode.SESSION_NOT_FOUND);
        }
        if (!session.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException(ErrorCode.SESSION_ACCESS_DENIED);
        }
        return session;
    }
    
    /**
     * 直接调用LLM生成回复
     */
    private String directLlmCall(AiAgent agent, String userInput, List<ChatMessage> history, String knowledgeContext) {
        List<com.soulmate.module.llm.dto.ChatMessage> messages = new ArrayList<>();
        
        String systemPrompt = agent.getSystemPrompt();
        if (knowledgeContext != null && !knowledgeContext.isEmpty()) {
            systemPrompt = systemPrompt + "\n\n" + knowledgeContext + 
                    "\n请基于以上参考知识回答用户的问题，如果参考知识中没有相关信息，则基于你的知识回答。";
        }
        messages.add(new com.soulmate.module.llm.dto.ChatMessage("system", systemPrompt));
        
        // 添加历史消息（过滤system消息避免重复）
        for (ChatMessage msg : history) {
            if (!"system".equals(msg.getRole())) {
                messages.add(new com.soulmate.module.llm.dto.ChatMessage(msg.getRole(), msg.getContent()));
            }
        }
        
        // 添加当前用户消息
        messages.add(new com.soulmate.module.llm.dto.ChatMessage("user", userInput));
        
        Map<String, Object> config = agent.getModelConfig() != null 
            ? JsonUtil.parse(agent.getModelConfig(), Map.class) 
            : null;
        
        try {
            return llmClient.chat(agent.getModelId(), messages, config);
        } catch (Exception e) {
            log.error("LLM调用失败: modelId={}, error={}", agent.getModelId(), e.getMessage(), e);
            throw new BusinessException(ErrorCode.LLM_CALL_ERROR, "AI服务暂时不可用，请稍后重试");
        }
    }
    
    /**
     * 根据用户输入生成会话标题
     */
    private String generateTitle(String userInput) {
        if (userInput == null || userInput.isEmpty()) {
            return "新对话";
        }
        String trimmed = userInput.trim();
        return trimmed.length() > 20 ? trimmed.substring(0, 20) + "..." : trimmed;
    }
}
