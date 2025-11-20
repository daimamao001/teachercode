package com.health.agent.module.chat.mapper;

import com.health.agent.module.chat.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 聊天消息Mapper
 * 
 * @author Health Agent Team
 * @date 2025-01-01
 */
@Mapper
public interface ChatMessageMapper {
    
    /**
     * 根据会话ID查询消息列表（分页）
     * 
     * @param sessionId 会话ID
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 消息列表
     */
    List<ChatMessage> findBySessionId(@Param("sessionId") Long sessionId, 
                                       @Param("offset") Integer offset, 
                                       @Param("limit") Integer limit);
    
    /**
     * 统计会话消息数量
     * 
     * @param sessionId 会话ID
     * @return 消息数量
     */
    Integer countBySessionId(@Param("sessionId") Long sessionId);
    
    /**
     * 插入消息
     * 
     * @param message 消息信息
     * @return 影响行数
     */
    int insert(ChatMessage message);
    
    /**
     * 根据会话ID删除所有消息
     * 
     * @param sessionId 会话ID
     * @return 影响行数
     */
    int deleteBySessionId(@Param("sessionId") Long sessionId);
    
    /**
     * 获取会话的最新N条消息（用于AI上下文）
     * 
     * @param sessionId 会话ID
     * @param limit 消息数量
     * @return 消息列表
     */
    List<ChatMessage> findLatestBySessionId(@Param("sessionId") Long sessionId, 
                                             @Param("limit") Integer limit);
}

