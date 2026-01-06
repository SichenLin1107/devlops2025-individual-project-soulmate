package com.soulmate.module.sensitive.mapper;

import com.soulmate.module.sensitive.entity.SysSensitiveWord;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 敏感词Mapper接口
 */
@Mapper
public interface SysSensitiveWordMapper {

    @Select("SELECT * FROM sys_sensitive_word WHERE id = #{id}")
    SysSensitiveWord selectById(@Param("id") Long id);

    @Select("SELECT * FROM sys_sensitive_word WHERE is_active = 1")
    List<SysSensitiveWord> selectActiveWords();

    @Select("SELECT * FROM sys_sensitive_word WHERE category = #{category} AND is_active = 1")
    List<SysSensitiveWord> selectActiveByCategory(@Param("category") String category);

    @Select("<script>" +
            "SELECT * FROM sys_sensitive_word " +
            "<where>" +
            "<if test='category != null and category != \"\"'> AND category = #{category}</if>" +
            "<if test='action != null and action != \"\"'> AND action = #{action}</if>" +
            "<if test='isActive != null'> AND is_active = #{isActive}</if>" +
            "<if test='keyword != null and keyword != \"\"'> AND word LIKE CONCAT('%', #{keyword}, '%')</if>" +
            "</where>" +
            "ORDER BY created_at DESC LIMIT #{offset}, #{size}" +
            "</script>")
    List<SysSensitiveWord> selectPage(@Param("offset") int offset, @Param("size") int size,
                                       @Param("category") String category,
                                       @Param("action") String action,
                                       @Param("isActive") Integer isActive,
                                       @Param("keyword") String keyword);

    @Select("<script>" +
            "SELECT COUNT(*) FROM sys_sensitive_word " +
            "<where>" +
            "<if test='category != null and category != \"\"'> AND category = #{category}</if>" +
            "<if test='action != null and action != \"\"'> AND action = #{action}</if>" +
            "<if test='isActive != null'> AND is_active = #{isActive}</if>" +
            "<if test='keyword != null and keyword != \"\"'> AND word LIKE CONCAT('%', #{keyword}, '%')</if>" +
            "</where>" +
            "</script>")
    long countByCondition(@Param("category") String category,
                          @Param("action") String action,
                          @Param("isActive") Integer isActive,
                          @Param("keyword") String keyword);

    @Insert("INSERT INTO sys_sensitive_word (word, category, action, replacement, is_active, created_at) " +
            "VALUES (#{word}, #{category}, #{action}, #{replacement}, #{isActive}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysSensitiveWord sensitiveWord);

    @Update("UPDATE sys_sensitive_word SET word = #{word}, category = #{category}, action = #{action}, " +
            "replacement = #{replacement}, is_active = #{isActive} WHERE id = #{id}")
    int update(SysSensitiveWord sensitiveWord);

    @Delete("DELETE FROM sys_sensitive_word WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
    
    @Update("<script>" +
            "UPDATE sys_sensitive_word SET is_active = #{isActive} WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    void batchUpdateStatus(@Param("ids") List<Long> ids, @Param("isActive") Integer isActive);
    
    @Delete("<script>" +
            "DELETE FROM sys_sensitive_word WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    void batchDelete(@Param("ids") List<Long> ids);
}

