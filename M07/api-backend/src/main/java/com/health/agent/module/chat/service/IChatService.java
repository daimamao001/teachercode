package com.health.agent.module.chat.service;

import com.health.agent.module.chat.vo.ChatMessageVO;
import com.health.agent.module.chat.vo.ChatSessionVO;

import java.util.List;

/**
 * 聊天服务接口
 * 
 * @author Health Agent Team
 * @date 2025-01-01
 */
public interface IChatService {
    
    /**
     * 创建会话
     * 
     * @param userId 用户ID
     * @param title 会话标题
     * @return 会话信息
     */
    ChatSessionVO createSession(Long userId, String title);
    
    /**
     * 发送消息并获取AI回复
     * 
     * @param sessionId 会话ID
     * @param content 消息内容
     * @return AI回复消息
     */
    ChatMessageVO sendMessage(Long sessionId, String content);
    
    /**
     * 获取用户会话列表（分页）
     * 
     * @param userId 用户ID
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @return 会话列表
     */
    List<ChatSessionVO> getUserSessions(Long userId, int page, int size);
    
    /**
     * 获取会话详情
     * 
     * @param sessionId 会话ID
     * @return 会话信息
     */
    ChatSessionVO getSessionById(Long sessionId);
    
    /**
     * 获取消息历史（分页）
     * 
     * @param sessionId 会话ID
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @return 消息列表
     */
    List<ChatMessageVO> getMessages(Long sessionId, int page, int size);
    
    /**
     * 删除会话
     * 
     * @param sessionId 会话ID
     * @return 是否成功
     */
    boolean deleteSession(Long sessionId);
    
    /**
     * 统计用户会话数
     * 
     * @param userId 用户ID
     * @return 会话数量
     */
    Integer countUserSessions(Long userId);
    
    /**
     * 统计会话消息数
     * 
     * @param sessionId 会话ID
     * @return 消息数量
     */
    Integer countSessionMessages(Long sessionId);
}

