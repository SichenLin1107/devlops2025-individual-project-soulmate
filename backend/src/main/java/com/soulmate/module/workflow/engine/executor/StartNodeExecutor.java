package com.soulmate.module.workflow.engine.executor;

import com.soulmate.module.workflow.dto.NodeOutput;
import com.soulmate.module.workflow.dto.NodesConfig;
import com.soulmate.module.workflow.dto.WorkflowContext;
import com.soulmate.module.workflow.engine.AbstractNodeExecutor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 开始节点执行器
 * 工作流的入口节点，将用户输入传递给后续节点
 */
@Component
public class StartNodeExecutor extends AbstractNodeExecutor {
    
    @Override
    public String getNodeType() {
        return "start";
    }
    
    @Override
    protected NodeOutput doExecute(NodesConfig.Node node, WorkflowContext context) {
        // 开始节点主要是初始化上下文，将用户输入作为输出传递
        Map<String, Object> data = new HashMap<>();
        data.put("user_input", context.getUserInput());
        data.put("session_id", context.getSessionId());
        data.put("agent_id", context.getAgentId());
        
        // 设置当前处理文本为用户输入（供后续节点使用）
        context.setVariable("current_text", context.getUserInput());
        
        return successOutput(data);
    }
}

