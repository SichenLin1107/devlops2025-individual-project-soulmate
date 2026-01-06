package com.soulmate.module.user.mapper;

import com.soulmate.module.user.entity.SysUser;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户数据访问接口
 */
@Mapper
public interface SysUserMapper {

    @Select("SELECT * FROM sys_user WHERE id = #{id}")
    SysUser selectById(@Param("id") String id);

    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    SysUser selectByUsername(@Param("username") String username);

    @Select("<script>" +
            "SELECT * FROM sys_user " +
            "<where>" +
            "<if test='role != null and role != \"\"'> AND role = #{role}</if>" +
            "<if test='status != null'> AND status = #{status}</if>" +
            "<if test='keyword != null and keyword != \"\"'> AND (username LIKE CONCAT('%', #{keyword}, '%') OR nickname LIKE CONCAT('%', #{keyword}, '%'))</if>" +
            "</where>" +
            "ORDER BY CASE WHEN role = 'superadmin' THEN 0 WHEN role = 'admin' THEN 1 ELSE 2 END, created_at DESC LIMIT #{offset}, #{size}" +
            "</script>")
    List<SysUser> selectPage(@Param("offset") int offset, @Param("size") int size,
                             @Param("role") String role, @Param("status") Integer status,
                             @Param("keyword") String keyword);

    @Select("<script>" +
            "SELECT COUNT(*) FROM sys_user " +
            "<where>" +
            "<if test='role != null and role != \"\"'> AND role = #{role}</if>" +
            "<if test='status != null'> AND status = #{status}</if>" +
            "<if test='keyword != null and keyword != \"\"'> AND (username LIKE CONCAT('%', #{keyword}, '%') OR nickname LIKE CONCAT('%', #{keyword}, '%'))</if>" +
            "</where>" +
            "</script>")
    long countByCondition(@Param("role") String role, @Param("status") Integer status,
                          @Param("keyword") String keyword);

    @Insert("INSERT INTO sys_user (id, username, password, role, nickname, avatar, bio, status, created_at, updated_at) " +
            "VALUES (#{id}, #{username}, #{password}, #{role}, #{nickname}, #{avatar}, #{bio}, #{status}, NOW(), NOW())")
    int insert(SysUser user);

    @Update("<script>" +
            "UPDATE sys_user SET " +
            "<if test='nickname != null'>nickname = #{nickname},</if>" +
            "<if test='avatar != null'>avatar = #{avatar},</if>" +
            "<if test='bio != null'>bio = #{bio},</if>" +
            "<if test='role != null'>role = #{role},</if>" +
            "<if test='status != null'>status = #{status},</if>" +
            "updated_at = NOW() " +
            "WHERE id = #{id}" +
            "</script>")
    int update(SysUser user);

    @Update("UPDATE sys_user SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") String id, @Param("status") Integer status);

    @Update("UPDATE sys_user SET password = #{password}, updated_at = NOW() WHERE id = #{id}")
    int updatePassword(@Param("id") String id, @Param("password") String password);

    @Delete("DELETE FROM sys_user WHERE id = #{id}")
    int deleteById(@Param("id") String id);
}

