package com.soulmate.module.knowledge.mapper;

import com.soulmate.module.knowledge.entity.AiKnowledgeBase;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 知识库数据访问接口
 */
@Mapper
public interface AiKnowledgeBaseMapper {

    @Select("SELECT * FROM ai_knowledge_base WHERE id = #{id}")
    AiKnowledgeBase selectById(@Param("id") String id);

    @Select("SELECT * FROM ai_knowledge_base ORDER BY created_at DESC")
    List<AiKnowledgeBase> selectAll();

    @Select("SELECT * FROM ai_knowledge_base WHERE created_by = #{createdBy} ORDER BY created_at DESC")
    List<AiKnowledgeBase> selectByCreator(@Param("createdBy") String createdBy);

    @Select("SELECT * FROM ai_knowledge_base ORDER BY created_at DESC LIMIT #{offset}, #{size}")
    List<AiKnowledgeBase> selectPage(@Param("offset") int offset, @Param("size") int size);

    @Select("SELECT COUNT(*) FROM ai_knowledge_base")
    long countAll();

    @Insert("INSERT INTO ai_knowledge_base (id, name, description, embedding_model, doc_count, segment_count, " +
            "is_active, created_by, created_at, updated_at) " +
            "VALUES (#{id}, #{name}, #{description}, #{embeddingModel}, #{docCount}, #{segmentCount}, " +
            "#{isActive}, #{createdBy}, NOW(), NOW())")
    int insert(AiKnowledgeBase kb);

    @Update("UPDATE ai_knowledge_base SET name = #{name}, description = #{description}, " +
            "embedding_model = #{embeddingModel}, updated_at = NOW() WHERE id = #{id}")
    int update(AiKnowledgeBase kb);

    @Update("UPDATE ai_knowledge_base SET doc_count = #{docCount}, segment_count = #{segmentCount}, " +
            "updated_at = NOW() WHERE id = #{id}")
    int updateCounts(@Param("id") String id, @Param("docCount") Integer docCount, @Param("segmentCount") Integer segmentCount);

    @Update("UPDATE ai_knowledge_base SET is_active = #{isActive}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") String id, @Param("isActive") Integer isActive);
    
    @Delete("DELETE FROM ai_knowledge_base WHERE id = #{id}")
    int deleteById(@Param("id") String id);
}

