package com.soulmate.module.llm.mapper;

import com.soulmate.module.llm.entity.LlmModel;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * LLM模型Mapper接口
 */
@Mapper
public interface LlmModelMapper {

    @Select("SELECT * FROM llm_model WHERE id = #{id}")
    LlmModel selectById(@Param("id") String id);

    @Select("SELECT * FROM llm_model WHERE is_active = 1")
    List<LlmModel> selectActiveModels();

    @Select("SELECT * FROM llm_model WHERE provider_id = #{providerId}")
    List<LlmModel> selectByProviderId(@Param("providerId") String providerId);

    @Select("SELECT * FROM llm_model WHERE model_type = #{modelType} AND is_active = 1")
    List<LlmModel> selectActiveByType(@Param("modelType") String modelType);

    @Select("SELECT * FROM llm_model ORDER BY created_at DESC")
    List<LlmModel> selectAll();
    
    @Select("<script>" +
            "SELECT * FROM llm_model " +
            "<where>" +
            "<if test='providerId != null'>AND provider_id = #{providerId}</if>" +
            "<if test='modelType != null'>AND model_type = #{modelType}</if>" +
            "<if test='isActive != null'>AND is_active = #{isActive}</if>" +
            "</where>" +
            "ORDER BY created_at DESC" +
            "</script>")
    List<LlmModel> selectByCondition(@Param("providerId") String providerId,
                                     @Param("modelType") String modelType,
                                     @Param("isActive") Integer isActive);

    @Insert("INSERT INTO llm_model (id, provider_id, name, display_name, model_type, api_base, api_key, " +
            "default_config, is_active, created_at, updated_at) " +
            "VALUES (#{id}, #{providerId}, #{name}, #{displayName}, #{modelType}, #{apiBase}, #{apiKey}, " +
            "#{defaultConfig}, #{isActive}, NOW(), NOW())")
    int insert(LlmModel model);

    @Update("UPDATE llm_model SET name = #{name}, display_name = #{displayName}, model_type = #{modelType}, " +
            "api_base = #{apiBase}, api_key = #{apiKey}, default_config = #{defaultConfig}, " +
            "is_active = #{isActive}, updated_at = NOW() WHERE id = #{id}")
    int update(LlmModel model);

    @Delete("DELETE FROM llm_model WHERE id = #{id}")
    int deleteById(@Param("id") String id);
}

