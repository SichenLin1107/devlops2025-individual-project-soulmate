package com.soulmate.module.workflow.mapper;

import com.soulmate.module.workflow.entity.AiWorkflow;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 工作流 Mapper 接口
 */
@Mapper
public interface AiWorkflowMapper {

    /**
     * 根据ID查询工作流
     */
    @Select("SELECT * FROM ai_workflow WHERE id = #{id}")
    AiWorkflow selectById(@Param("id") String id);

    /**
     * 查询所有工作流
     */
    @Select("SELECT * FROM ai_workflow ORDER BY created_at DESC")
    List<AiWorkflow> selectAll();

    /**
     * 查询启用的工作流
     */
    @Select("SELECT * FROM ai_workflow WHERE is_active = 1 ORDER BY created_at DESC")
    List<AiWorkflow> selectActiveWorkflows();

    /**
     * 按创建者查询工作流
     */
    @Select("SELECT * FROM ai_workflow WHERE created_by = #{createdBy} ORDER BY created_at DESC")
    List<AiWorkflow> selectByCreator(@Param("createdBy") String createdBy);

    /**
     * 查询工作流列表（分页）
     */
    @Select("SELECT * FROM ai_workflow ORDER BY created_at DESC LIMIT #{offset}, #{size}")
    List<AiWorkflow> selectPage(@Param("offset") int offset, @Param("size") int size);

    /**
     * 统计工作流总数
     */
    @Select("SELECT COUNT(*) FROM ai_workflow")
    long countAll();

    /**
     * 插入工作流
     */
    @Insert("INSERT INTO ai_workflow (id, name, description, nodes_config, status, validation_status, " +
            "node_count, node_types, has_rag, has_crisis_intervention, is_active, created_by, created_at, updated_at) " +
            "VALUES (#{id}, #{name}, #{description}, #{nodesConfig}, #{status}, #{validationStatus}, " +
            "#{nodeCount}, #{nodeTypes}, #{hasRag}, #{hasCrisisIntervention}, #{isActive}, #{createdBy}, NOW(), NOW())")
    int insert(AiWorkflow workflow);

    /**
     * 更新工作流
     */
    @Update("UPDATE ai_workflow SET name = #{name}, description = #{description}, nodes_config = #{nodesConfig}, " +
            "status = #{status}, validation_status = #{validationStatus}, node_count = #{nodeCount}, " +
            "node_types = #{nodeTypes}, has_rag = #{hasRag}, has_crisis_intervention = #{hasCrisisIntervention}, " +
            "is_active = #{isActive}, updated_at = NOW() WHERE id = #{id}")
    int update(AiWorkflow workflow);

    /**
     * 更新工作流状态
     */
    @Update("UPDATE ai_workflow SET is_active = #{isActive}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") String id, @Param("isActive") Integer isActive);

    /**
     * 删除工作流
     */
    @Delete("DELETE FROM ai_workflow WHERE id = #{id}")
    int deleteById(@Param("id") String id);
}

