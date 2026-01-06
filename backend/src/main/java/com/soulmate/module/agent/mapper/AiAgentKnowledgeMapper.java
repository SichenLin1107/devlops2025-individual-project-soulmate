package com.soulmate.module.agent.mapper;

import com.soulmate.module.agent.entity.AiAgentKnowledge;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 智能体-知识库关联Mapper接口
 */
@Mapper
public interface AiAgentKnowledgeMapper {

    /**
     * 根据智能体ID查询关联的知识库
     */
    @Select("SELECT * FROM ai_agent_knowledge WHERE agent_id = #{agentId}")
    List<AiAgentKnowledge> selectByAgentId(@Param("agentId") String agentId);

    /**
     * 根据知识库ID查询关联的智能体
     */
    @Select("SELECT * FROM ai_agent_knowledge WHERE kb_id = #{kbId}")
    List<AiAgentKnowledge> selectByKbId(@Param("kbId") String kbId);

    /**
     * 插入关联关系
     */
    @Insert("INSERT INTO ai_agent_knowledge (agent_id, kb_id, created_at) VALUES (#{agentId}, #{kbId}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AiAgentKnowledge agentKnowledge);

    /**
     * 删除关联关系
     */
    @Delete("DELETE FROM ai_agent_knowledge WHERE agent_id = #{agentId} AND kb_id = #{kbId}")
    int delete(@Param("agentId") String agentId, @Param("kbId") String kbId);

    /**
     * 根据智能体ID删除所有关联关系
     */
    @Delete("DELETE FROM ai_agent_knowledge WHERE agent_id = #{agentId}")
    int deleteByAgentId(@Param("agentId") String agentId);

    /**
     * 根据知识库ID删除所有关联关系
     */
    @Delete("DELETE FROM ai_agent_knowledge WHERE kb_id = #{kbId}")
    int deleteByKbId(@Param("kbId") String kbId);
}

