package com.soulmate.module.agent.mapper;

import com.soulmate.module.agent.entity.AiAgent;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 智能体Mapper接口
 */
@Mapper
public interface AiAgentMapper {

    /**
     * 根据ID查询智能体
     */
    @Select("SELECT * FROM ai_agent WHERE id = #{id}")
    AiAgent selectById(@Param("id") String id);

    /**
     * 查询所有智能体
     */
    @Select("SELECT * FROM ai_agent ORDER BY created_at DESC")
    List<AiAgent> selectAll();

    /**
     * 分页查询已上架的智能体
     */
    @Select("<script>" +
            "SELECT * FROM ai_agent WHERE status = 'published' " +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (name LIKE CONCAT('%', #{keyword}, '%') " +
            "     OR description LIKE CONCAT('%', #{keyword}, '%') " +
            "     OR tags LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if> " +
            "<if test='tag != null and tag != \"\"'> " +
            "AND tags LIKE CONCAT('%', #{tag}, '%') " +
            "</if> " +
            "ORDER BY " +
            "<choose> " +
            "<when test='sort != null and sort == \"createTime\"'> created_at DESC </when> " +
            "<when test='sort != null and sort == \"name\"'> name ASC </when> " +
            "<otherwise> heat_value DESC </otherwise> " +
            "</choose> " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<AiAgent> selectPublishedAgentsPage(@Param("offset") int offset, 
                                             @Param("size") int size,
                                             @Param("keyword") String keyword,
                                             @Param("tag") String tag,
                                             @Param("sort") String sort);

    /**
     * 统计已上架智能体数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM ai_agent WHERE status = 'published' " +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (name LIKE CONCAT('%', #{keyword}, '%') " +
            "     OR description LIKE CONCAT('%', #{keyword}, '%') " +
            "     OR tags LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if> " +
            "<if test='tag != null and tag != \"\"'> " +
            "AND tags LIKE CONCAT('%', #{tag}, '%') " +
            "</if>" +
            "</script>")
    long countPublishedAgents(@Param("keyword") String keyword, @Param("tag") String tag);

    /**
     * 分页查询智能体列表
     */
    @Select("<script>" +
            "SELECT * FROM ai_agent " +
            "<if test='status != null and status != \"\"'> WHERE status = #{status} </if> " +
            "<if test='keyword != null and keyword != \"\"'> " +
            "<choose> " +
            "<when test='status != null and status != \"\"'> AND </when> " +
            "<otherwise> WHERE </otherwise> " +
            "</choose> " +
            "(name LIKE CONCAT('%', #{keyword}, '%') " +
            " OR description LIKE CONCAT('%', #{keyword}, '%') " +
            " OR tags LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if> " +
            "ORDER BY " +
            "<choose> " +
            "<when test='sort != null and sort == \"createTime\"'> created_at DESC </when> " +
            "<when test='sort != null and sort == \"heat\"'> heat_value DESC </when> " +
            "<when test='sort != null and sort == \"name\"'> name ASC </when> " +
            "<otherwise> created_at DESC </otherwise> " +
            "</choose> " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<AiAgent> selectPage(@Param("offset") int offset, 
                             @Param("size") int size,
                             @Param("status") String status,
                             @Param("keyword") String keyword,
                             @Param("sort") String sort);

    /**
     * 统计智能体总数
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM ai_agent " +
            "<if test='status != null and status != \"\"'> WHERE status = #{status} </if> " +
            "<if test='keyword != null and keyword != \"\"'> " +
            "<choose> " +
            "<when test='status != null and status != \"\"'> AND </when> " +
            "<otherwise> WHERE </otherwise> " +
            "</choose> " +
            "(name LIKE CONCAT('%', #{keyword}, '%') " +
            " OR description LIKE CONCAT('%', #{keyword}, '%') " +
            " OR tags LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "</script>")
    long countAll(@Param("status") String status, @Param("keyword") String keyword);

    /**
     * 插入智能体
     */
    @Insert("INSERT INTO ai_agent (id, name, avatar, description, tags, greeting, system_prompt, model_id, " +
            "model_config, workflow_id, status, heat_value, created_by, created_at, updated_at) " +
            "VALUES (#{id}, #{name}, #{avatar}, #{description}, #{tags}, #{greeting}, #{systemPrompt}, #{modelId}, " +
            "#{modelConfig}, #{workflowId}, #{status}, #{heatValue}, #{createdBy}, NOW(), NOW())")
    int insert(AiAgent agent);

    /**
     * 更新智能体
     */
    @Update("UPDATE ai_agent SET name = #{name}, avatar = #{avatar}, description = #{description}, tags = #{tags}, " +
            "greeting = #{greeting}, system_prompt = #{systemPrompt}, model_id = #{modelId}, model_config = #{modelConfig}, " +
            "workflow_id = #{workflowId}, updated_at = NOW() WHERE id = #{id}")
    int update(AiAgent agent);

    /**
     * 更新智能体状态
     */
    @Update("UPDATE ai_agent SET status = #{status}, published_at = IF(#{status}='published', NOW(), published_at), " +
            "updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") String id, @Param("status") String status);

    /**
     * 增加热度值
     */
    @Update("UPDATE ai_agent SET heat_value = heat_value + 1, updated_at = NOW() WHERE id = #{id}")
    int incrementHeatValue(@Param("id") String id);

    /**
     * 删除智能体
     */
    @Delete("DELETE FROM ai_agent WHERE id = #{id}")
    int deleteById(@Param("id") String id);
    
    /**
     * 根据工作流ID查询智能体列表
     */
    @Select("SELECT * FROM ai_agent WHERE workflow_id = #{workflowId}")
    List<AiAgent> selectByWorkflowId(@Param("workflowId") String workflowId);
    
    /**
     * 根据模型ID查询智能体列表
     */
    @Select("SELECT * FROM ai_agent WHERE model_id = #{modelId}")
    List<AiAgent> selectByModelId(@Param("modelId") String modelId);
    
    /**
     * 批量更新智能体状态
     */
    @Update("<script>" +
            "UPDATE ai_agent SET status = #{status}, updated_at = NOW() WHERE id IN " +
            "<foreach collection='agentIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateStatus(@Param("agentIds") List<String> agentIds, @Param("status") String status);
}

