package com.soulmate.module.workflow.engine;

import com.soulmate.module.workflow.dto.NodeOutput;
import com.soulmate.module.workflow.dto.NodesConfig;
import com.soulmate.module.workflow.dto.WorkflowContext;

/**
 * 节点执行器接口
 * 每种节点类型实现此接口
 */
public interface NodeExecutor {
    
    /**
     * 获取支持的节点类型
     */
    String getNodeType();
    
    /**
     * 执行节点
     * @param node 节点配置
     * @param context 工作流上下文
     * @return 节点输出
     */
    NodeOutput execute(NodesConfig.Node node, WorkflowContext context);
}

