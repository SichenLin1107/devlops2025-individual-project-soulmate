package com.soulmate.module.workflow.engine.executor;

import com.soulmate.module.knowledge.dto.RetrievalResult;
import com.soulmate.module.knowledge.entity.AiKnowledgeBase;
import com.soulmate.module.knowledge.mapper.AiKnowledgeBaseMapper;
import com.soulmate.module.knowledge.service.RagClient;
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
 * RAG检索节点执行器
 * 从知识库中检索相关内容
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagRetrievalExecutor extends AbstractNodeExecutor {
    
    private final RagClient ragClient;
    private final AiKnowledgeBaseMapper knowledgeBaseMapper;
    
    @Override
    public String getNodeType() {
        return "rag_retrieval";
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected NodeOutput doExecute(NodesConfig.Node node, WorkflowContext context) {
        log.info("【RAG节点】开始执行RAG检索节点: nodeId={}", node.getId());
        
        String queryText = (String) context.getVariable("current_text");
        if (queryText == null) {
            queryText = context.getUserInput();
        }
        log.info("【RAG节点】查询文本: {}", queryText);
        
        // 获取节点配置（同时支持驼峰和下划线命名）
        List<String> kbIds = getConfig(node, "kb_ids", Collections.emptyList());
        // 如果是单个知识库ID（knowledgeBaseId），转换为列表
        if (kbIds == null || kbIds.isEmpty()) {
            String singleKbId = getConfig(node, "knowledgeBaseId", null);
            if (singleKbId != null && !singleKbId.isEmpty()) {
                kbIds = Collections.singletonList(singleKbId);
            }
        }
        Integer topK = getConfig(node, "topK", 3);
        Double minScore = getConfig(node, "scoreThreshold", 0.5);
        
        log.info("【RAG节点】节点配置: kbIds={}, topK={}, minScore={}", kbIds, topK, minScore);
        
        // 如果没有配置知识库，尝试从上下文获取智能体关联的知识库
        if (kbIds == null || kbIds.isEmpty()) {
            Object contextKbIds = context.getVariable("agent_kb_ids");
            log.info("【RAG节点】节点未配置知识库，从上下文获取: agent_kb_ids={}", contextKbIds);
            if (contextKbIds instanceof List) {
                kbIds = (List<String>) contextKbIds;
                log.info("【RAG节点】从上下文获取到知识库: {}", kbIds);
            }
        }
        
        if (kbIds == null || kbIds.isEmpty()) {
            log.warn("【RAG节点】未配置知识库，跳过检索");
            return skippedOutput("未配置知识库");
        }
        
        // 执行检索
        List<RetrievalResult> allResults = new ArrayList<>();
        List<String> searchedKbs = new ArrayList<>();
        Set<String> searchedKbNames = new HashSet<>(); // 用于存储知识库名称
        
        log.info("【RAG节点】开始检索知识库: kbIds={}, query={}", kbIds, queryText);
        
        for (String kbId : kbIds) {
            try {
                log.info("【RAG节点】检索知识库: kbId={}", kbId);
                List<RetrievalResult> results = ragClient.search(kbId, queryText, topK);
                log.info("【RAG节点】检索返回: kbId={}, resultCount={}", kbId, results != null ? results.size() : 0);
                
                if (results != null && !results.isEmpty()) {
                    allResults.addAll(results);
                    searchedKbs.add(kbId);
                    
                    // 获取知识库名称
                    try {
                        AiKnowledgeBase kb = knowledgeBaseMapper.selectById(kbId);
                        if (kb != null && kb.getName() != null) {
                            searchedKbNames.add(kb.getName());
                            log.info("【RAG节点】获取知识库名称: kbId={}, kbName={}", kbId, kb.getName());
                        } else {
                            log.warn("【RAG节点】知识库不存在: kbId={}", kbId);
                        }
                    } catch (Exception e) {
                        log.warn("【RAG节点】获取知识库名称失败: kbId={}, error={}", kbId, e.getMessage());
                    }
                    
                    log.info("【RAG节点】知识库检索成功: kbId={}, count={}, scores={}", 
                            kbId, results.size(), 
                            results.stream().map(r -> r.getScore() != null ? String.format("%.3f", r.getScore()) : "null")
                                    .collect(Collectors.joining(", ")));
                } else {
                    log.warn("【RAG节点】知识库检索无结果: kbId={}", kbId);
                }
            } catch (Exception e) {
                log.error("【RAG节点】知识库检索失败: kbId={}, error={}", kbId, e.getMessage(), e);
            }
        }
        
        log.info("【RAG节点】检索完成: 总结果数={}, 知识库名称={}", allResults.size(), searchedKbNames);
        
        // 按相关度排序并筛选
        List<RetrievalResult> filteredResults = allResults.stream()
                .filter(r -> r.getScore() == null || r.getScore() >= minScore)
                .sorted((a, b) -> Double.compare(
                        b.getScore() != null ? b.getScore() : 0.0,
                        a.getScore() != null ? a.getScore() : 0.0))
                .limit(topK)
                .collect(Collectors.toList());
        
        log.info("【RAG节点】筛选后结果: 原始结果数={}, 筛选后结果数={}, 阈值={}", 
                allResults.size(), filteredResults.size(), minScore);
        
        // 构建本节点的知识上下文
        String ragKnowledgeContext = "";
        if (!filteredResults.isEmpty()) {
            StringBuilder contextBuilder = new StringBuilder();
            contextBuilder.append("【相关参考知识】\n");
            for (int i = 0; i < filteredResults.size(); i++) {
                RetrievalResult result = filteredResults.get(i);
                contextBuilder.append(String.format("%d. %s\n", i + 1, result.getText()));
            }
            ragKnowledgeContext = contextBuilder.toString();
        }
        
        // ========== 核心改动：合并智能体知识上下文和RAG节点知识上下文 ==========
        // 获取已有的智能体知识上下文
        String agentKnowledgeContext = (String) context.getVariable("agent_knowledge_context");
        String existingKnowledgeContext = (String) context.getVariable("knowledge_context");
        
        String finalKnowledgeContext;
        if (ragKnowledgeContext.isEmpty()) {
            // RAG节点没有检索到结果，保持原有知识上下文
            finalKnowledgeContext = existingKnowledgeContext != null ? existingKnowledgeContext : "";
        } else if (agentKnowledgeContext != null && !agentKnowledgeContext.isEmpty()) {
            // 合并智能体知识上下文和RAG节点检索结果
            finalKnowledgeContext = agentKnowledgeContext + "\n\n【工作流检索补充】\n" + ragKnowledgeContext;
            log.info("【RAG节点】合并知识上下文: 智能体知识长度={}, RAG知识长度={}", 
                    agentKnowledgeContext.length(), ragKnowledgeContext.length());
        } else {
            // 只有RAG节点的检索结果
            finalKnowledgeContext = ragKnowledgeContext;
        }
        
        // 更新上下文
        context.setVariable("knowledge_context", finalKnowledgeContext);
        context.setVariable("retrieval_results", filteredResults);
        context.setVariable("has_knowledge", !finalKnowledgeContext.isEmpty());
        
        // 将知识库名称列表添加到metadata中，供后续使用
        if (!searchedKbNames.isEmpty()) {
            // 获取现有的referenced_kbs列表（可能来自其他RAG节点）
            @SuppressWarnings("unchecked")
            List<String> existingKbNames = (List<String>) context.getMetadata().get("referenced_kbs");
            if (existingKbNames != null) {
                // 合并现有列表和新列表
                Set<String> mergedKbNames = new HashSet<>(existingKbNames);
                mergedKbNames.addAll(searchedKbNames);
                context.getMetadata().put("referenced_kbs", new ArrayList<>(mergedKbNames));
                log.info("【RAG节点】合并知识库引用: 现有={}, 新增={}, 合并后={}", 
                        existingKbNames, searchedKbNames, mergedKbNames);
            } else {
                // 首次设置
                context.getMetadata().put("referenced_kbs", new ArrayList<>(searchedKbNames));
                log.info("【RAG节点】设置知识库引用到metadata: {}", searchedKbNames);
            }
        } else {
            log.warn("【RAG节点】检索到结果但知识库名称为空，无法设置引用");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("query", queryText);
        data.put("searched_kbs", searchedKbs);
        data.put("result_count", filteredResults.size());
        data.put("knowledge_context", finalKnowledgeContext);
        data.put("results", filteredResults.stream()
                .map(r -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("text", r.getText());
                    m.put("score", r.getScore());
                    m.put("doc_id", r.getDocId());
                    return m;
                })
                .collect(Collectors.toList()));
        
        log.info("RAG检索完成: kbCount={}, resultCount={}, kbNames={}", 
                searchedKbs.size(), filteredResults.size(), searchedKbNames);
        
        return successOutput(data);
    }
}

