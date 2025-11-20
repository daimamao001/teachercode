package com.health.agent.module.chat.mapper;

import com.health.agent.module.chat.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 聊天会话Mapper
 * 
 * @author Health Agent Team
 * @date 2025-01-01
 */
@Mapper
public interface ChatSessionMapper {
    
    /**
     * 根据ID查询会话
     * 
     * @param id 会话ID
     * @return 会话信息
     */
    ChatSession findById(@Param("id") Long id);
    
    /**
     * 根据用户ID查询会话列表（分页）
     * 
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 会话列表
     */
    List<ChatSession> findByUserId(@Param("userId") Long userId, 
                                    @Param("offset") Integer offset, 
                                    @Param("limit") Integer limit);
    
    /**
     * 统计用户会话数量
     * 
     * @param userId 用户ID
     * @return 会话数量
     */
    Integer countByUserId(@Param("userId") Long userId);
    
    /**
     * 插入会话
     * 
     * @param session 会话信息
     * @return 影响行数
     */
    int insert(ChatSession session);
    
    /**
     * 更新会话
     * 
     * @param session 会话信息
     * @return 影响行数
     */
    int update(ChatSession session);
    
    /**
     * 删除会话（软删除）
     * 
     * @param id 会话ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);
}

