package com.soulmate.module.knowledge.mapper;

import com.soulmate.module.knowledge.entity.AiKnowledgeDocument;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 知识库文档数据访问接口
 */
@Mapper
public interface AiKnowledgeDocumentMapper {

    @Select("SELECT * FROM ai_knowledge_document WHERE id = #{id}")
    AiKnowledgeDocument selectById(@Param("id") Long id);

    @Select("SELECT * FROM ai_knowledge_document WHERE kb_id = #{kbId} ORDER BY created_at DESC")
    List<AiKnowledgeDocument> selectByKbId(@Param("kbId") String kbId);

    @Select("<script>" +
            "SELECT * FROM ai_knowledge_document WHERE kb_id = #{kbId} " +
            "<if test='status != null and status != \"\"'> AND status = #{status} </if> " +
            "ORDER BY created_at DESC LIMIT #{offset}, #{size}" +
            "</script>")
    List<AiKnowledgeDocument> selectByKbIdWithStatus(@Param("kbId") String kbId, 
                                                      @Param("status") String status,
                                                      @Param("offset") int offset, 
                                                      @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM ai_knowledge_document WHERE kb_id = #{kbId} " +
            "<if test='status != null and status != \"\"'> AND status = #{status} </if>" +
            "</script>")
    long countByKbIdWithStatus(@Param("kbId") String kbId, @Param("status") String status);

    @Select("SELECT COUNT(*) FROM ai_knowledge_document WHERE kb_id = #{kbId}")
    long countByKbId(@Param("kbId") String kbId);

    @Select("SELECT * FROM ai_knowledge_document WHERE status = 'pending' ORDER BY created_at ASC")
    List<AiKnowledgeDocument> selectPendingDocuments();

    @Insert("INSERT INTO ai_knowledge_document (kb_id, file_name, file_type, file_size, file_path, " +
            "segment_count, status, created_at) " +
            "VALUES (#{kbId}, #{fileName}, #{fileType}, #{fileSize}, #{filePath}, #{segmentCount}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AiKnowledgeDocument document);

    @Update("UPDATE ai_knowledge_document SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    @Update("UPDATE ai_knowledge_document SET status = #{status}, retry_count = #{retryCount}, " +
            "error_message = #{errorMessage}, updated_at = NOW() WHERE id = #{id}")
    int updateRetry(@Param("id") Long id, @Param("status") String status, 
                    @Param("retryCount") Integer retryCount, @Param("errorMessage") String errorMessage);
    
    @Update("UPDATE ai_knowledge_document SET segment_count = #{segmentCount}, updated_at = NOW() WHERE id = #{id}")
    int updateSegmentCount(@Param("id") Long id, @Param("segmentCount") Integer segmentCount);

    @Delete("DELETE FROM ai_knowledge_document WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Delete("DELETE FROM ai_knowledge_document WHERE kb_id = #{kbId}")
    int deleteByKbId(@Param("kbId") String kbId);
}

