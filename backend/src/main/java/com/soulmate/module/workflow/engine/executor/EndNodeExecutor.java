package com.soulmate.module.workflow.engine.executor;

import com.soulmate.module.workflow.dto.NodeOutput;
import com.soulmate.module.workflow.dto.NodesConfig;
import com.soulmate.module.workflow.dto.WorkflowContext;
import com.soulmate.module.workflow.engine.AbstractNodeExecutor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 结束节点执行器
 * 工作流的出口节点，汇总最终结果
 */
@Component
public class EndNodeExecutor extends AbstractNodeExecutor {
    
    @Override
    public String getNodeType() {
        return "end";
    }
    
    @Override
    protected NodeOutput doExecute(NodesConfig.Node node, WorkflowContext context) {
        Map<String, Object> data = new HashMap<>();
        
        // 收集最终回复
        String finalResponse = context.getFinalResponse();
        if (finalResponse == null || finalResponse.isEmpty()) {
            // 如果没有设置最终回复，尝试从LLM节点输出获取
            for (Map.Entry<String, NodeOutput> entry : context.getNodeOutputs().entrySet()) {
                NodeOutput output = entry.getValue();
                if ("llm_process".equals(output.getNodeType()) && output.getData() != null) {
                    Object response = output.getData().get("response");
                    if (response != null) {
                        finalResponse = response.toString();
                        break;
                    }
                }
            }
        }
        
        data.put("final_response", finalResponse);
        data.put("emotion", context.getEmotion());
        data.put("is_crisis", context.isCrisis());
        
        // 确保最终回复设置到上下文中
        if (finalResponse != null) {
            context.setFinalResponse(finalResponse);
        }
        
        return successOutput(data);
    }
}

