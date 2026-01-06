package com.soulmate.module.workflow.engine.executor;

import com.soulmate.module.workflow.dto.NodeOutput;
import com.soulmate.module.workflow.dto.NodesConfig;
import com.soulmate.module.workflow.dto.WorkflowContext;
import com.soulmate.module.workflow.engine.AbstractNodeExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 危机干预节点执行器
 * 当检测到用户可能处于心理危机状态时，提供干预响应
 */
@Slf4j
@Component
public class CrisisInterventionExecutor extends AbstractNodeExecutor {
    
    // 默认危机干预消息
    private static final String DEFAULT_CRISIS_MESSAGE = """
            我注意到您可能正在经历一些困难的时刻。请记住，您并不孤单，专业的帮助随时可以获得。
            
            如果您正在经历困扰，请记住：
            - 您的感受是真实的，值得被关注
            - 寻求帮助是勇敢的表现
            - 专业的心理咨询师可以帮助您
            
            如果您需要专业支持，可以拨打心理援助热线：
            📞 全国心理援助热线：400-161-9995
            📞 北京心理危机研究与干预中心：010-82951332
            📞 生命热线：400-821-1215
            
            我会一直在这里陪伴您。请告诉我，我能为您做些什么？
            """;
    
    @Override
    public String getNodeType() {
        return "crisis_intervention";
    }
    
    @Override
    protected NodeOutput doExecute(NodesConfig.Node node, WorkflowContext context) {
        log.info("执行危机干预节点: sessionId={}, agentId={}", 
                context.getSessionId(), context.getAgentId());
        
        // 获取节点配置
        String customMessage = getConfig(node, "customMessage", null);
        Boolean showHotline = getConfig(node, "showHotline", true);
        String hotlineNumber = getConfig(node, "hotlineNumber", "400-161-9995");
        Boolean notifyAdmin = getConfig(node, "notifyAdmin", true);
        String interventionLevel = getConfig(node, "interventionLevel", "standard");
        
        // 构建干预响应
        String responseMessage;
        if (customMessage != null && !customMessage.isEmpty()) {
            responseMessage = customMessage;
        } else {
            responseMessage = DEFAULT_CRISIS_MESSAGE;
        }
        
        // 如果需要，添加热线信息
        if (showHotline && hotlineNumber != null && !responseMessage.contains(hotlineNumber)) {
            responseMessage += "\n\n如需紧急帮助，请拨打：" + hotlineNumber;
        }
        
        // 设置最终响应（危机干预响应优先级最高）
        context.setFinalResponse(responseMessage);
        context.setCrisis(true);
        
        // 设置标记，表示已执行危机干预
        context.setVariable("crisis_intervention_executed", true);
        context.setVariable("intervention_level", interventionLevel);
        
        if (notifyAdmin) {
            log.warn("危机干预触发，需要通知管理员: sessionId={}, agentId={}", 
                    context.getSessionId(), context.getAgentId());
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("intervention_level", interventionLevel);
        data.put("message", responseMessage);
        data.put("hotline_shown", showHotline);
        data.put("admin_notified", notifyAdmin);
        
        log.info("危机干预响应已生成: level={}", interventionLevel);
        
        return successOutput(data);
    }
}

