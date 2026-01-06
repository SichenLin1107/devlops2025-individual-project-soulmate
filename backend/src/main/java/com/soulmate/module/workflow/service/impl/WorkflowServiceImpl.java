package com.soulmate.module.workflow.service.impl;

import com.soulmate.common.BusinessException;
import com.soulmate.common.ErrorCode;
import com.soulmate.common.PageResult;
import com.soulmate.module.agent.entity.AiAgent;
import com.soulmate.module.agent.mapper.AiAgentMapper;
import com.soulmate.module.workflow.dto.WorkflowRequest;
import com.soulmate.module.workflow.dto.WorkflowVO;
import com.soulmate.module.workflow.entity.AiWorkflow;
import com.soulmate.module.workflow.mapper.AiWorkflowMapper;
import com.soulmate.module.workflow.service.WorkflowService;
import com.soulmate.security.UserContext;
import com.soulmate.util.IdGenerator;
import com.soulmate.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 工作流服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {
    
    private final AiWorkflowMapper workflowMapper;
    private final AiAgentMapper agentMapper;
    
    @Override
    public PageResult<WorkflowVO> listWorkflows(int page, int size, String keyword, Integer isActive) {
        int offset = (page - 1) * size;
        List<AiWorkflow> workflows = workflowMapper.selectPage(offset, size);
        long total = workflowMapper.countAll();
        
        List<WorkflowVO> list = workflows.stream()
                .map(WorkflowVO::fromEntitySimple)
                .collect(Collectors.toList());
        
        return PageResult.of(list, total, page, size);
    }
    
    @Override
    public WorkflowVO getWorkflow(String id) {
        AiWorkflow workflow = workflowMapper.selectById(id);
        if (workflow == null) {
            throw new BusinessException(ErrorCode.WORKFLOW_NOT_FOUND);
        }
        return WorkflowVO.fromEntity(workflow);
    }
    
    @Override
    public String createWorkflow(WorkflowRequest request) {
        AiWorkflow workflow = new AiWorkflow();
        workflow.setId(IdGenerator.workflowId());
        workflow.setName(request.getName());
        workflow.setDescription(request.getDescription());
        workflow.setNodesConfig(JsonUtil.toJson(request.getNodesConfig()));
        workflow.setIsActive(request.getIsActive());
        workflow.setCreatedBy(UserContext.getUserId());
        
        // 计算元数据字段
        computeWorkflowMetadata(workflow, request.getNodesConfig());
        
        // 新建时设置默认状态为草稿
        if (workflow.getStatus() == null) {
            workflow.setStatus("draft");
        }
        
        workflowMapper.insert(workflow);
        log.info("工作流创建成功: id={}, name={}", workflow.getId(), workflow.getName());
        return workflow.getId();
    }
    
    @Override
    public void updateWorkflow(String id, WorkflowRequest request) {
        AiWorkflow workflow = workflowMapper.selectById(id);
        if (workflow == null) {
            throw new BusinessException(ErrorCode.WORKFLOW_NOT_FOUND);
        }
        
        workflow.setName(request.getName());
        workflow.setDescription(request.getDescription());
        workflow.setNodesConfig(JsonUtil.toJson(request.getNodesConfig()));
        workflow.setIsActive(request.getIsActive());
        
        // 更新元数据字段
        computeWorkflowMetadata(workflow, request.getNodesConfig());
        
        workflowMapper.update(workflow);
        log.info("工作流更新成功: id={}", id);
    }
    
    /**
     * 计算工作流元数据字段（节点数量、节点类型、是否包含RAG/安全检测等）
     */
    @SuppressWarnings("unchecked")
    private void computeWorkflowMetadata(AiWorkflow workflow, Map<String, Object> nodesConfig) {
        if (nodesConfig == null) {
            workflow.setNodeCount(0);
            workflow.setNodeTypes(null);
            workflow.setHasRag(0);
            workflow.setHasCrisisIntervention(0);
            workflow.setValidationStatus("invalid");
            return;
        }
        
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) nodesConfig.get("nodes");
        if (nodes == null || nodes.isEmpty()) {
            workflow.setNodeCount(0);
            workflow.setNodeTypes(null);
            workflow.setHasRag(0);
            workflow.setHasCrisisIntervention(0);
            workflow.setValidationStatus("invalid");
            return;
        }
        
        // 计算节点数量
        workflow.setNodeCount(nodes.size());
        
        // 收集节点类型
        Set<String> nodeTypeSet = new HashSet<>();
        boolean hasRag = false;
        boolean hasCrisisIntervention = false;
        boolean hasStart = false;
        boolean hasEnd = false;
        
        for (Map<String, Object> node : nodes) {
            String type = (String) node.get("type");
            if (type != null) {
                nodeTypeSet.add(type);
                
                // 检查特殊节点类型
                switch (type) {
                    case "rag_retrieval":
                        hasRag = true;
                        break;
                    case "safety_check":
                        hasCrisisIntervention = true;
                        break;
                    case "start":
                        hasStart = true;
                        break;
                    case "end":
                        hasEnd = true;
                        break;
                }
            }
        }
        
        workflow.setNodeTypes(JsonUtil.toJson(new ArrayList<>(nodeTypeSet)));
        workflow.setHasRag(hasRag ? 1 : 0);
        workflow.setHasCrisisIntervention(hasCrisisIntervention ? 1 : 0);
        
        // 简单验证：必须有开始和结束节点
        if (hasStart && hasEnd) {
            workflow.setValidationStatus("valid");
            workflow.setStatus("published");
        } else {
            workflow.setValidationStatus("invalid");
            workflow.setStatus("draft");
        }
    }
    
    @Override
    public void deleteWorkflow(String id) {
        AiWorkflow workflow = workflowMapper.selectById(id);
        if (workflow == null) {
            throw new BusinessException(ErrorCode.WORKFLOW_NOT_FOUND);
        }
        
        workflowMapper.deleteById(id);
        log.info("工作流删除成功: id={}", id);
    }
    
    @Override
    @Transactional
    public void updateWorkflowStatus(String id, Integer isActive, boolean disableRelatedAgents) {
        AiWorkflow workflow = workflowMapper.selectById(id);
        if (workflow == null) {
            throw new BusinessException(ErrorCode.WORKFLOW_NOT_FOUND);
        }
        
        // 如果要禁用，检查关联的智能体
        if (isActive != null && isActive == 0) {
            List<AiAgent> relatedAgents = agentMapper.selectByWorkflowId(id);
            if (!relatedAgents.isEmpty()) {
                // 如果用户确认禁用关联的智能体，则禁用它们
                if (disableRelatedAgents) {
                    List<String> agentIds = relatedAgents.stream()
                            .map(AiAgent::getId)
                            .collect(Collectors.toList());
                    agentMapper.batchUpdateStatus(agentIds, "offline");
                    log.info("禁用工作流时，同时禁用了 {} 个关联的智能体: workflowId={}, agentIds={}", 
                            agentIds.size(), id, agentIds);
                }
            }
        }
        
        workflowMapper.updateStatus(id, isActive);
        log.info("工作流状态更新: id={}, isActive={}", id, isActive);
    }
    
    @Override
    public int countRelatedAgents(String id) {
        List<AiAgent> relatedAgents = agentMapper.selectByWorkflowId(id);
        return relatedAgents.size();
    }
    
    @Override
    public List<com.soulmate.module.workflow.dto.NodeDefinition> getNodeDefinitions() {
        // 返回所有可用的节点类型定义
        List<com.soulmate.module.workflow.dto.NodeDefinition> definitions = new ArrayList<>();
        
        // 开始节点 - 无需配置
        definitions.add(com.soulmate.module.workflow.dto.NodeDefinition.builder()
                .type("start")
                .name("开始")
                .icon("Promotion")
                .color("#8FBC8F")
                .category("control")
                .inputs(List.of())
                .outputs(List.of(
                        com.soulmate.module.workflow.dto.NodeDefinition.PortDefinition.builder()
                                .id("output").name("输出").dataType("object")
                                .description("用户输入消息").build()
                ))
                .configSchema(Map.of()) // 开始节点无需配置
                .build());
        
        // 文本处理节点 - 可配置预处理选项
        definitions.add(com.soulmate.module.workflow.dto.NodeDefinition.builder()
                .type("text_process")
                .name("文本处理")
                .icon("Document")
                .color("#9CB4CC")
                .category("process")
                .inputs(List.of(
                        com.soulmate.module.workflow.dto.NodeDefinition.PortDefinition.builder()
                                .id("input").name("输入").dataType("string").required(true).build()
                ))
                .outputs(List.of(
                        com.soulmate.module.workflow.dto.NodeDefinition.PortDefinition.builder()
                                .id("output").name("输出").dataType("string").build()
                ))
                .configSchema(Map.of(
                        "trimSpaces", Map.of(
                                "type", "boolean",
                                "label", "去除首尾空格",
                                "description", "自动去除文本首尾的空白字符",
                                "default", true
                        ),
                        "removeEmoji", Map.of(
                                "type", "boolean",
                                "label", "移除表情符号",
                                "description", "移除文本中的Emoji表情",
                                "default", false
                        ),
                        "maxLength", Map.of(
                                "type", "integer",
                                "label", "最大长度",
                                "description", "限制输入文本的最大字符数，0表示不限制",
                                "default", 2000,
                                "min", 0,
                                "max", 10000
                        ),
                        "normalizeWhitespace", Map.of(
                                "type", "boolean",
                                "label", "规范化空白",
                                "description", "将多个连续空格压缩为单个空格",
                                "default", true
                        )
                ))
                .build());
        
        // 安全检测节点 - 可配置检测级别和敏感词类别
        definitions.add(com.soulmate.module.workflow.dto.NodeDefinition.builder()
                .type("safety_check")
                .name("安全检测")
                .icon("Warning")
                .color("#E8C49A")
                .category("safety")
                .inputs(List.of(
                        com.soulmate.module.workflow.dto.NodeDefinition.PortDefinition.builder()
                                .id("input").name("输入").dataType("string").required(true).build()
                ))
                .outputs(List.of(
                        com.soulmate.module.workflow.dto.NodeDefinition.PortDefinition.builder()
                                .id("safe").name("安全").dataType("object")
                                .description("检测通过时的输出").build(),
                        com.soulmate.module.workflow.dto.NodeDefinition.PortDefinition.builder()
                                .id("crisis").name("危机").dataType("object")
                                .description("检测到风险时的输出").build()
                ))
                .configSchema(Map.of(
                        "checkLevel", Map.of(
                                "type", "string",
                                "label", "检测级别",
                                "description", "安全检测的严格程度",
                                "default", "standard",
                                "options", List.of(
                                        Map.of("value", "loose", "label", "宽松 - 仅检测严重风险"),
                                        Map.of("value", "standard", "label", "标准 - 平衡检测"),
                                        Map.of("value", "strict", "label", "严格 - 全面检测")
                                )
                        ),
                        "enableCrisisIntervention", Map.of(
                                "type", "boolean",
                                "label", "启用危机干预",
                                "description", "检测到自杀/自伤倾向时触发危机干预流程",
                                "default", true
                        ),
                        "sensitiveCategories", Map.of(
                                "type", "array",
                                "label", "敏感词类别",
                                "description", "选择需要检测的敏感词类别",
                                "default", List.of("politics", "violence", "adult", "advertising"),
                                "options", List.of(
                                        Map.of("value", "politics", "label", "政治敏感"),
                                        Map.of("value", "violence", "label", "暴力血腥"),
                                        Map.of("value", "adult", "label", "色情低俗"),
                                        Map.of("value", "advertising", "label", "广告推销"),
                                        Map.of("value", "gambling", "label", "赌博诈骗"),
                                        Map.of("value", "drugs", "label", "毒品违禁"),
                                        Map.of("value", "discrimination", "label", "歧视侮辱"),
                                        Map.of("value", "selfharm", "label", "自残自杀")
                                )
                        ),
                        "customBlockList", Map.of(
                                "type", "string",
                                "format", "textarea",
                                "label", "自定义屏蔽词",
                                "description", "额外的屏蔽词列表，每行一个",
                                "placeholder", "每行输入一个词语",
                                "rows", 4
                        )
                ))
                .build());
        
        // 情绪识别节点
        definitions.add(com.soulmate.module.workflow.dto.NodeDefinition.builder()
                .type("emotion_recognition")
                .name("情绪识别")
                .icon("ChatDotRound")
                .color("#7EB8C9")
                .category("ai")
                .inputs(List.of(
                        com.soulmate.module.workflow.dto.NodeDefinition.PortDefinition.builder()
                                .id("input").name("输入").dataType("string").required(true).build()
                ))
                .outputs(List.of(
                        com.soulmate.module.workflow.dto.NodeDefinition.PortDefinition.builder()
                                .id("output").name("输出").dataType("object")
                                .description("包含情绪标签和置信度").build()
                ))
                .configSchema(Map.of(
                        "modelType", Map.of(
                                "type", "string",
                                "label", "识别模型",
                                "description", "选择用于情绪识别的模型",
                                "default", "bert",
                                "options", List.of(
                                        Map.of("value", "bert", "label", "BERT模型 - 准确度高"),
                                        Map.of("value", "lstm", "label", "LSTM模型 - 速度快"),
                                        Map.of("value", "rules", "label", "规则匹配 - 简单场景")
                                )
                        ),
                        "confidenceThreshold", Map.of(
                                "type", "number",
                                "label", "置信度阈值",
                                "description", "低于此阈值的识别结果将标记为'未知'",
                                "default", 0.6,
                                "min", 0.0,
                                "max", 1.0,
                                "step", 0.1
                        ),
                        "emotionCategories", Map.of(
                                "type", "array",
                                "label", "情绪类别",
                                "description", "需要识别的情绪类别",
                                "default", List.of("happy", "sad", "angry", "anxious", "neutral")
                        )
                ))
                .build());
        
        // RAG检索节点 - 可配置知识库和检索参数
        definitions.add(com.soulmate.module.workflow.dto.NodeDefinition.builder()
                .type("rag_retrieval")
                .name("知识检索")
                .icon("Search")
                .color("#A8C5D8")
                .category("retrieval")
                .inputs(List.of(
                        com.soulmate.module.workflow.dto.NodeDefinition.PortDefinition.builder()
                                .id("input").name("查询").dataType("string").required(true).build()
                ))
                .outputs(List.of(
                        com.soulmate.module.workflow.dto.NodeDefinition.PortDefinition.builder()
                                .id("output").name("检索结果").dataType("object")
                                .description("返回相关知识片段列表").build()
                ))
                .configSchema(Map.of(
                        "knowledgeBaseId", Map.of(
                                "type", "string",
                                "label", "选择知识库",
                                "description", "选择要检索的知识库",
                                "placeholder", "请选择知识库",
                                "required", true
                        ),
                        "topK", Map.of(
                                "type", "integer",
                                "label", "返回数量",
                                "description", "最多返回多少条相关结果",
                                "default", 5,
                                "min", 1,
                                "max", 20
                        ),
                        "similarityThreshold", Map.of(
                                "type", "number",
                                "label", "相似度阈值",
                                "description", "低于此相似度的结果将被过滤",
                                "default", 0.7,
                                "min", 0.0,
                                "max", 1.0,
                                "step", 0.05
                        ),
                        "useRerank", Map.of(
                                "type", "boolean",
                                "label", "启用重排序",
                                "description", "使用Rerank模型对检索结果重新排序",
                                "default", false
                        )
                ))
                .build());
        
        // LLM处理节点 - 可配置模型和提示词
        definitions.add(com.soulmate.module.workflow.dto.NodeDefinition.builder()
                .type("llm_process")
                .name("LLM处理")
                .icon("EditPen")
                .color("#7EB8C9")
                .category("llm")
                .inputs(List.of(
                        com.soulmate.module.workflow.dto.NodeDefinition.PortDefinition.builder()
                                .id("input").name("输入").dataType("object").required(true).build()
                ))
                .outputs(List.of(
                        com.soulmate.module.workflow.dto.NodeDefinition.PortDefinition.builder()
                                .id("output").name("输出").dataType("object")
                                .description("LLM生成的回复").build()
                ))
                .configSchema(Map.of(
                        "modelId", Map.of(
                                "type", "string",
                                "label", "选择模型",
                                "description", "选择要使用的LLM模型",
                                "placeholder", "请选择模型",
                                "required", false
                        ),
                        "systemPrompt", Map.of(
                                "type", "string",
                                "format", "textarea",
                                "label", "系统提示词",
                                "description", "设定AI的角色和行为规范",
                                "placeholder", "你是一个温暖、善解人意的心理咨询师...",
                                "rows", 6
                        ),
                        "temperature", Map.of(
                                "type", "number",
                                "label", "温度",
                                "description", "控制输出的随机性，值越高越有创意",
                                "default", 0.7,
                                "min", 0.0,
                                "max", 2.0,
                                "step", 0.1
                        ),
                        "maxTokens", Map.of(
                                "type", "integer",
                                "label", "最大Token数",
                                "description", "生成回复的最大长度",
                                "default", 1024,
                                "min", 100,
                                "max", 4096
                        ),
                        "useContext", Map.of(
                                "type", "boolean",
                                "label", "使用对话上下文",
                                "description", "是否将历史消息作为上下文传入模型",
                                "default", true
                        ),
                        "contextWindow", Map.of(
                                "type", "integer",
                                "label", "上下文窗口",
                                "description", "传入多少条历史消息作为上下文",
                                "default", 10,
                                "min", 1,
                                "max", 50
                        )
                ))
                .build());
        
        // 危机干预节点 - 处理检测到的心理危机情况
        definitions.add(com.soulmate.module.workflow.dto.NodeDefinition.builder()
                .type("crisis_intervention")
                .name("危机干预")
                .icon("Warning")
                .color("#D4A5A5")
                .category("safety")
                .inputs(List.of(
                        com.soulmate.module.workflow.dto.NodeDefinition.PortDefinition.builder()
                                .id("input").name("危机信息").dataType("object").required(true)
                                .description("接收安全检测识别的危机信息").build()
                ))
                .outputs(List.of(
                        com.soulmate.module.workflow.dto.NodeDefinition.PortDefinition.builder()
                                .id("output").name("干预回复").dataType("object")
                                .description("输出危机干预的专业回复").build()
                ))
                .configSchema(Map.of(
                        "interventionLevel", Map.of(
                                "type", "string",
                                "label", "干预级别",
                                "description", "危机干预的响应级别",
                                "default", "standard",
                                "options", List.of(
                                        Map.of("value", "mild", "label", "轻度 - 提供情绪支持和建议"),
                                        Map.of("value", "standard", "label", "标准 - 提供专业资源引导"),
                                        Map.of("value", "urgent", "label", "紧急 - 立即提供危机热线")
                                )
                        ),
                        "showHotline", Map.of(
                                "type", "boolean",
                                "label", "显示求助热线",
                                "description", "在回复中自动包含心理援助热线",
                                "default", true
                        ),
                        "hotlineNumber", Map.of(
                                "type", "string",
                                "label", "求助热线号码",
                                "description", "心理援助热线电话号码",
                                "default", "400-161-9995"
                        ),
                        "notifyAdmin", Map.of(
                                "type", "boolean",
                                "label", "通知管理员",
                                "description", "检测到危机时自动通知系统管理员",
                                "default", true
                        ),
                        "customMessage", Map.of(
                                "type", "string",
                                "format", "textarea",
                                "label", "自定义干预消息",
                                "description", "可自定义危机干预时的回复模板",
                                "placeholder", "我注意到您现在可能遇到了一些困难，我想让您知道，您并不孤单...",
                                "rows", 4
                        )
                ))
                .build());
        
        // 回复优化节点 - 对AI生成的回复进行最终处理和优化
        definitions.add(com.soulmate.module.workflow.dto.NodeDefinition.builder()
                .type("post_process")
                .name("回复优化")
                .icon("Setting")
                .color("#B8AFA0")
                .category("process")
                .inputs(List.of(
                        com.soulmate.module.workflow.dto.NodeDefinition.PortDefinition.builder()
                                .id("input").name("AI回复").dataType("object").required(true)
                                .description("接收LLM生成的原始回复").build()
                ))
                .outputs(List.of(
                        com.soulmate.module.workflow.dto.NodeDefinition.PortDefinition.builder()
                                .id("output").name("优化后回复").dataType("object")
                                .description("输出经过优化处理的最终回复").build()
                ))
                .configSchema(Map.of(
                        "formatResponse", Map.of(
                                "type", "boolean",
                                "label", "格式化回复",
                                "description", "自动整理文本格式，如去除多余空行、规范标点等",
                                "default", true
                        ),
                        "addDisclaimer", Map.of(
                                "type", "boolean",
                                "label", "添加免责声明",
                                "description", "在回复末尾自动添加心理咨询免责提示",
                                "default", false
                        ),
                        "disclaimerText", Map.of(
                                "type", "string",
                                "format", "textarea",
                                "label", "免责声明内容",
                                "description", "自定义免责声明文本，仅在启用免责声明时生效",
                                "placeholder", "温馨提示：以上建议仅供参考，如有需要请咨询专业心理咨询师。",
                                "rows", 3
                        ),
                        "logResponse", Map.of(
                                "type", "boolean",
                                "label", "记录对话日志",
                                "description", "将本次对话内容保存到日志中，便于后续分析",
                                "default", true
                        )
                ))
                .build());
        
        // 结束节点 - 无需配置
        definitions.add(com.soulmate.module.workflow.dto.NodeDefinition.builder()
                .type("end")
                .name("结束")
                .icon("Finished")
                .color("#D4A5A5")
                .category("control")
                .inputs(List.of(
                        com.soulmate.module.workflow.dto.NodeDefinition.PortDefinition.builder()
                                .id("input").name("输入").dataType("object").required(true).build()
                ))
                .outputs(List.of())
                .configSchema(Map.of()) // 结束节点无需配置
                .build());
        
        return definitions;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> validateWorkflow(Map<String, Object> nodesConfig) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (nodesConfig == null) {
            errors.add("工作流配置不能为空");
            return Map.of("valid", false, "errors", errors, "warnings", warnings);
        }
        
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) nodesConfig.get("nodes");
        List<Map<String, Object>> edges = (List<Map<String, Object>>) nodesConfig.get("edges");
        
        if (nodes == null || nodes.isEmpty()) {
            errors.add("工作流必须包含至少一个节点");
            return Map.of("valid", false, "errors", errors, "warnings", warnings);
        }
        
        // 检查必须节点
        boolean hasStart = false;
        boolean hasEnd = false;
        Set<String> nodeIds = new HashSet<>();
        
        for (Map<String, Object> node : nodes) {
            String type = (String) node.get("type");
            String id = (String) node.get("id");
            
            if (id != null) {
                nodeIds.add(id);
            }
            
            if ("start".equals(type)) hasStart = true;
            if ("end".equals(type)) hasEnd = true;
        }
        
        if (!hasStart) {
            errors.add("工作流必须包含开始节点");
        }
        if (!hasEnd) {
            errors.add("工作流必须包含结束节点");
        }
        
        // 检查边的有效性
        if (edges != null) {
            for (Map<String, Object> edge : edges) {
                String source = (String) edge.get("source");
                String target = (String) edge.get("target");
                
                if (source != null && !nodeIds.contains(source)) {
                    errors.add("连线源节点 '" + source + "' 不存在");
                }
                if (target != null && !nodeIds.contains(target)) {
                    errors.add("连线目标节点 '" + target + "' 不存在");
                }
            }
        }
        
        // 警告检查
        if (nodes.size() == 2 && hasStart && hasEnd) {
            warnings.add("工作流只有开始和结束节点，建议添加处理节点");
        }
        
        boolean valid = errors.isEmpty();
        return Map.of("valid", valid, "errors", errors, "warnings", warnings);
    }
}

