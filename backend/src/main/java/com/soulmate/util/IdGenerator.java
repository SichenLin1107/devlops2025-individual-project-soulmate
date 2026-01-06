package com.soulmate.util;

import java.util.UUID;

/**
 * ID生成器，生成带前缀的业务ID
 */
public final class IdGenerator {

    private IdGenerator() {}

    /**
     * 生成用户ID
     */
    public static String userId() {
        return "usr_" + shortUuid();
    }

    /**
     * 生成智能体ID
     */
    public static String agentId() {
        return "agt_" + shortUuid();
    }

    /**
     * 生成知识库ID
     */
    public static String knowledgeBaseId() {
        return "kb_" + shortUuid();
    }

    /**
     * 生成工作流ID
     */
    public static String workflowId() {
        return "wfl_" + shortUuid();
    }

    /**
     * 生成LLM提供商ID
     */
    public static String providerId() {
        return "prv_" + shortUuid();
    }

    /**
     * 生成LLM模型ID
     */
    public static String modelId() {
        return "mdl_" + shortUuid();
    }

    /**
     * 生成短UUID（8位）
     */
    public static String shortUuid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    /**
     * 生成完整UUID（32位，无横线）
     */
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

