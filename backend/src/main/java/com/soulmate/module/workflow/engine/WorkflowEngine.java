package com.soulmate.module.workflow.engine;

import com.soulmate.module.workflow.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 工作流执行引擎
 * 负责解析工作流配置、按条件分支执行节点
 */
@Slf4j
@Service
public class WorkflowEngine {
    
    private final Map<String, NodeExecutor> executorMap;
    
    /**
     * 构造函数：自动注入所有NodeExecutor实现
     */
    @Autowired
    public WorkflowEngine(List<NodeExecutor> executors) {
        this.executorMap = new HashMap<>();
        for (NodeExecutor executor : executors) {
            this.executorMap.put(executor.getNodeType(), executor);
            log.info("注册节点执行器: {}", executor.getNodeType());
        }
    }
    
    /**
     * 执行工作流
     * @param nodesConfig 节点配置
     * @param context 执行上下文
     * @return 执行结果
     */
    public WorkflowResult execute(NodesConfig nodesConfig, WorkflowContext context) {
        long startTime = System.currentTimeMillis();
        List<String> executedNodes = new ArrayList<>();
        
        try {
            log.info("开始执行工作流: sessionId={}, agentId={}", 
                    context.getSessionId(), context.getAgentId());
            
            // 1. 构建节点映射和边映射
            Map<String, NodesConfig.Node> nodeMap = new HashMap<>();
            Map<String, List<NodesConfig.Edge>> outgoingEdges = new HashMap<>();
            
            for (NodesConfig.Node node : nodesConfig.getNodes()) {
                nodeMap.put(node.getId(), node);
                outgoingEdges.put(node.getId(), new ArrayList<>());
            }
            
            if (nodesConfig.getEdges() != null) {
                for (NodesConfig.Edge edge : nodesConfig.getEdges()) {
                    outgoingEdges.get(edge.getSource()).add(edge);
                }
            }
            
            // 2. 找到开始节点
            String currentNodeId = findStartNode(nodesConfig);
            if (currentNodeId == null) {
                throw new RuntimeException("工作流中没有找到开始节点");
            }
            
            // 3. 按分支逻辑执行节点
            Set<String> visited = new HashSet<>();
            
            while (currentNodeId != null && !visited.contains(currentNodeId)) {
                visited.add(currentNodeId);
                
                NodesConfig.Node node = nodeMap.get(currentNodeId);
                if (node == null) {
                    log.warn("节点不存在: {}", currentNodeId);
                    break;
                }
                
                // 检查是否需要提前终止（如安全检测被阻止）
                if (shouldTerminate(context)) {
                    log.info("工作流提前终止: nodeId={}", currentNodeId);
                    break;
                }
                
                // 获取执行器
                NodeExecutor executor = executorMap.get(node.getType());
                if (executor == null) {
                    log.warn("未找到节点执行器: type={}, 跳过节点", node.getType());
                    // 尝试找到下一个节点继续
                    currentNodeId = findNextNode(currentNodeId, outgoingEdges, context);
                    continue;
                }
                
                // 执行节点
                log.debug("执行节点: id={}, type={}", node.getId(), node.getType());
                NodeOutput output = executor.execute(node, context);
                context.setNodeOutput(currentNodeId, output);
                executedNodes.add(currentNodeId);
                
                // 检查节点执行失败
                if ("failed".equals(output.getStatus())) {
                    log.error("节点执行失败: nodeId={}, error={}", currentNodeId, output.getError());
                    if (isCriticalNode(node.getType())) {
                        break;
                    }
                }
                
                // 如果是结束节点，退出
                if ("end".equals(node.getType())) {
                    break;
                }
                
                // 根据当前节点类型和上下文，选择下一个节点
                currentNodeId = findNextNode(currentNodeId, outgoingEdges, context);
            }
            
            // 4. 构建执行结果
            long duration = System.currentTimeMillis() - startTime;
            
            WorkflowResult result = WorkflowResult.builder()
                    .response(context.getFinalResponse())
                    .emotion(context.getEmotion())
                    .isCrisis(context.isCrisis())
                    .nodesExecuted(executedNodes)
                    .metadata(buildMetadata(context, duration))
                    .build();
            
            log.info("工作流执行完成: duration={}ms, nodesExecuted={}", duration, executedNodes);
            
            return result;
            
        } catch (Exception e) {
            log.error("工作流执行异常: error={}", e.getMessage(), e);
            
            return WorkflowResult.builder()
                    .response("抱歉，系统处理遇到问题，请稍后重试。")
                    .isCrisis(false)
                    .nodesExecuted(executedNodes)
                    .metadata(Map.of(
                            "error", e.getMessage(),
                            "duration_ms", System.currentTimeMillis() - startTime
                    ))
                    .build();
        }
    }
    
    /**
     * 找到开始节点
     */
    private String findStartNode(NodesConfig config) {
        for (NodesConfig.Node node : config.getNodes()) {
            if ("start".equals(node.getType())) {
                return node.getId();
            }
        }
        // 如果没有明确的start节点，返回第一个节点
        return config.getNodes().isEmpty() ? null : config.getNodes().get(0).getId();
    }
    
    /**
     * 根据条件找到下一个要执行的节点
     */
    private String findNextNode(String currentNodeId, 
                                 Map<String, List<NodesConfig.Edge>> outgoingEdges,
                                 WorkflowContext context) {
        List<NodesConfig.Edge> edges = outgoingEdges.get(currentNodeId);
        if (edges == null || edges.isEmpty()) {
            return null;
        }
        
        // 获取安全检测状态
        Boolean isCrisis = context.isCrisis();
        Object safetyAction = context.getVariable("safety_action");
        
        log.debug("查找下一节点: currentNode={}, isCrisis={}, safetyAction={}, edgeCount={}", 
                currentNodeId, isCrisis, safetyAction, edges.size());
        
        // 如果只有一条边，直接返回
        if (edges.size() == 1) {
            return edges.get(0).getTarget();
        }
        
        // 如果有多条边，根据条件选择
        for (NodesConfig.Edge edge : edges) {
            String sourcePort = edge.getSourcePort();
            
            // 处理安全检测节点的分支
            if (sourcePort != null) {
                // 危机分支：当检测到危机状态时执行
                if ("crisis".equals(sourcePort)) {
                    if (Boolean.TRUE.equals(isCrisis) || "crisis_intervention".equals(safetyAction)) {
                        log.debug("选择危机分支: target={}", edge.getTarget());
                        return edge.getTarget();
                    }
                    continue; // 不是危机状态，跳过这条边
                }
                
                // 安全分支：当没有危机且通过安全检测时执行
                if ("safe".equals(sourcePort) || "output".equals(sourcePort)) {
                    if (!Boolean.TRUE.equals(isCrisis) && !"crisis_intervention".equals(safetyAction)) {
                        log.debug("选择安全分支: target={}", edge.getTarget());
                        return edge.getTarget();
                    }
                    continue; // 是危机状态，跳过这条边
                }
                
                // 阻止分支
                if ("blocked".equals(sourcePort)) {
                    if ("block".equals(safetyAction)) {
                        log.debug("选择阻止分支: target={}", edge.getTarget());
                        return edge.getTarget();
                    }
                    continue;
                }
            }
        }
        
        // 如果没有匹配的条件分支，返回第一条边的目标（默认路径）
        // 但要避免返回crisis分支作为默认路径
        for (NodesConfig.Edge edge : edges) {
            String sourcePort = edge.getSourcePort();
            if (sourcePort == null || "output".equals(sourcePort) || "safe".equals(sourcePort)) {
                return edge.getTarget();
            }
        }
        
        // 如果都是条件分支且都不匹配，返回第一条
        return edges.get(0).getTarget();
    }
    
    /**
     * 检查是否需要提前终止
     */
    private boolean shouldTerminate(WorkflowContext context) {
        // 如果安全检测要求阻止
        Object safetyAction = context.getVariable("safety_action");
        if ("block".equals(safetyAction)) {
            // 设置阻止响应
            context.setFinalResponse("抱歉，您的消息包含不适当的内容，无法处理。");
            return true;
        }
        return false;
    }
    
    /**
     * 判断是否为关键节点
     */
    private boolean isCriticalNode(String nodeType) {
        return "llm_process".equals(nodeType) || "start".equals(nodeType);
    }
    
    /**
     * 构建元数据
     */
    private Map<String, Object> buildMetadata(WorkflowContext context, long duration) {
        Map<String, Object> metadata = new HashMap<>(context.getMetadata());
        metadata.put("duration_ms", duration);
        metadata.put("has_knowledge", context.getVariable("has_knowledge"));
        metadata.put("safety_passed", context.getVariable("safety_passed"));
        return metadata;
    }
}
