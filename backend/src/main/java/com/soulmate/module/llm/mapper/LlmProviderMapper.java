package com.soulmate.module.llm.mapper;

import com.soulmate.module.llm.entity.LlmProvider;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * LLM提供商Mapper接口
 */
@Mapper
public interface LlmProviderMapper {

    @Select("SELECT * FROM llm_provider WHERE id = #{id}")
    LlmProvider selectById(@Param("id") String id);

    @Select("SELECT * FROM llm_provider WHERE is_active = 1")
    List<LlmProvider> selectActiveProviders();

    @Select("SELECT * FROM llm_provider ORDER BY created_at DESC")
    List<LlmProvider> selectAll();

    @Insert("INSERT INTO llm_provider (id, name, api_base, api_key, is_active, created_at, updated_at) " +
            "VALUES (#{id}, #{name}, #{apiBase}, #{apiKey}, #{isActive}, NOW(), NOW())")
    int insert(LlmProvider provider);

    @Update("UPDATE llm_provider SET name = #{name}, api_base = #{apiBase}, api_key = #{apiKey}, " +
            "is_active = #{isActive}, updated_at = NOW() WHERE id = #{id}")
    int update(LlmProvider provider);

    @Delete("DELETE FROM llm_provider WHERE id = #{id}")
    int deleteById(@Param("id") String id);
}

