package com.soulmate.module.knowledge.mapper;

import com.soulmate.module.knowledge.entity.AiKnowledgeSegment;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 知识切片数据访问接口
 */
@Mapper
public interface AiKnowledgeSegmentMapper {

    @Select("SELECT * FROM ai_knowledge_segment WHERE id = #{id}")
    AiKnowledgeSegment selectById(@Param("id") Long id);

    @Select("SELECT * FROM ai_knowledge_segment WHERE kb_id = #{kbId} AND status = 1 ORDER BY position ASC")
    List<AiKnowledgeSegment> selectByKbId(@Param("kbId") String kbId);

    @Select("SELECT * FROM ai_knowledge_segment WHERE doc_id = #{docId} AND status = 1 ORDER BY position ASC")
    List<AiKnowledgeSegment> selectByDocId(@Param("docId") Long docId);

    @Insert("<script>" +
            "INSERT INTO ai_knowledge_segment (kb_id, doc_id, content, vector_id, word_count, position, status, created_at, updated_at) VALUES " +
            "<foreach collection='segments' item='seg' separator=','>" +
            "(#{seg.kbId}, #{seg.docId}, #{seg.content}, #{seg.vectorId}, #{seg.wordCount}, #{seg.position}, #{seg.status}, NOW(), NOW())" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("segments") List<AiKnowledgeSegment> segments);

    @Update("UPDATE ai_knowledge_segment SET vector_id = #{vectorId}, updated_at = NOW() WHERE id = #{id}")
    int updateVectorId(@Param("id") Long id, @Param("vectorId") String vectorId);

    @Update("UPDATE ai_knowledge_segment SET status = 0, updated_at = NOW() WHERE id = #{id}")
    int softDelete(@Param("id") Long id);

    @Update("UPDATE ai_knowledge_segment SET status = 0, updated_at = NOW() WHERE doc_id = #{docId}")
    int softDeleteByDocId(@Param("docId") Long docId);

    @Delete("DELETE FROM ai_knowledge_segment WHERE kb_id = #{kbId}")
    int deleteByKbId(@Param("kbId") String kbId);

    @Select("SELECT COUNT(*) FROM ai_knowledge_segment WHERE kb_id = #{kbId} AND status = 1")
    long countByKbId(@Param("kbId") String kbId);
}

