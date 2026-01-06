package com.soulmate.module.chat.mapper;

import com.soulmate.module.chat.entity.ChatSession;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 会话数据访问接口
 */
@Mapper
public interface ChatSessionMapper {
    /**
     * 根据ID查询会话
     */
    @Select("SELECT * FROM chat_session WHERE id = #{id}")
    ChatSession selectById(@Param("id") Long id);

    /**
     * 按用户ID查询会话列表（置顶优先，按更新时间倒序）
     */
    @Select("SELECT * FROM chat_session WHERE user_id = #{userId} ORDER BY is_pinned DESC, updated_at DESC")
    List<ChatSession> selectByUserId(@Param("userId") String userId);

    /**
     * 按用户ID和智能体ID查询会话
     */
    @Select("SELECT * FROM chat_session WHERE user_id = #{userId} AND agent_id = #{agentId} ORDER BY updated_at DESC")
    List<ChatSession> selectByUserAndAgent(@Param("userId") String userId, @Param("agentId") String agentId);

    /**
     * 分页查询会话列表（支持agentId和sessionType过滤）
     */
    @Select("<script>" +
            "SELECT * FROM chat_session WHERE user_id = #{userId} " +
            "<if test='agentId != null and agentId != \"\"'> AND agent_id = #{agentId} </if> " +
            "<if test='sessionType != null and sessionType != \"\"'> AND session_type = #{sessionType} </if> " +
            "ORDER BY is_pinned DESC, updated_at DESC LIMIT #{offset}, #{size}" +
            "</script>")
    List<ChatSession> selectPage(@Param("userId") String userId,
                                 @Param("agentId") String agentId,
                                 @Param("sessionType") String sessionType,
                                 @Param("offset") int offset,
                                 @Param("size") int size);

    /**
     * 统计用户会话数（支持agentId和sessionType过滤）
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM chat_session WHERE user_id = #{userId} " +
            "<if test='agentId != null and agentId != \"\"'> AND agent_id = #{agentId} </if> " +
            "<if test='sessionType != null and sessionType != \"\"'> AND session_type = #{sessionType} </if>" +
            "</script>")
    long countByUserId(@Param("userId") String userId,
                       @Param("agentId") String agentId,
                       @Param("sessionType") String sessionType);

    /**
     * 插入会话
     */
    @Insert("INSERT INTO chat_session (user_id, agent_id, title, session_type, is_pinned, message_count, created_at, updated_at) " +
            "VALUES (#{userId}, #{agentId}, #{title}, #{sessionType}, #{isPinned}, #{messageCount}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ChatSession session);

    /**
     * 更新会话标题
     */
    @Update("UPDATE chat_session SET title = #{title}, updated_at = NOW() WHERE id = #{id}")
    int updateTitle(@Param("id") Long id, @Param("title") String title);

    /**
     * 更新置顶状态
     */
    @Update("UPDATE chat_session SET is_pinned = #{isPinned}, updated_at = NOW() WHERE id = #{id}")
    int updatePinned(@Param("id") Long id, @Param("isPinned") Integer isPinned);

    /**
     * 更新消息数和活跃时间
     */
    @Update("UPDATE chat_session SET message_count = #{messageCount}, updated_at = NOW() WHERE id = #{id}")
    int updateMessageCount(@Param("id") Long id, @Param("messageCount") Integer messageCount);

    /**
     * 删除会话
     */
    @Delete("DELETE FROM chat_session WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}

