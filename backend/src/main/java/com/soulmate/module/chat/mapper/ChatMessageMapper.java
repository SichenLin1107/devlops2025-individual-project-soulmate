package com.soulmate.module.chat.mapper;

import com.soulmate.module.chat.entity.ChatMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 消息数据访问接口
 */
@Mapper
public interface ChatMessageMapper {
    /**
     * 根据ID查询消息
     */
    @Select("SELECT * FROM chat_message WHERE id = #{id}")
    ChatMessage selectById(@Param("id") Long id);

    /**
     * 按会话ID查询消息（按时间正序）
     */
    @Select("SELECT * FROM chat_message WHERE session_id = #{sessionId} ORDER BY created_at ASC")
    List<ChatMessage> selectBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 查询最近N条消息用于上下文
     */
    @Select("SELECT * FROM (SELECT * FROM chat_message WHERE session_id = #{sessionId} ORDER BY created_at DESC LIMIT #{limit}) t ORDER BY created_at ASC")
    List<ChatMessage> selectRecentMessages(@Param("sessionId") Long sessionId, @Param("limit") int limit);

    /**
     * 分页查询消息列表
     */
    @Select("SELECT * FROM chat_message WHERE session_id = #{sessionId} ORDER BY created_at ASC LIMIT #{offset}, #{size}")
    List<ChatMessage> selectPage(@Param("sessionId") Long sessionId, @Param("offset") int offset, @Param("size") int size);

    /**
     * 统计会话消息数
     */
    @Select("SELECT COUNT(*) FROM chat_message WHERE session_id = #{sessionId}")
    long countBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 插入消息
     */
    @Insert("INSERT INTO chat_message (session_id, role, content, msg_type, emotion, token_count, created_at) " +
            "VALUES (#{sessionId}, #{role}, #{content}, #{msgType}, #{emotion}, #{tokenCount}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ChatMessage message);

    /**
     * 根据ID删除消息
     */
    @Delete("DELETE FROM chat_message WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 按会话ID删除所有消息
     */
    @Delete("DELETE FROM chat_message WHERE session_id = #{sessionId}")
    int deleteBySessionId(@Param("sessionId") Long sessionId);
}

