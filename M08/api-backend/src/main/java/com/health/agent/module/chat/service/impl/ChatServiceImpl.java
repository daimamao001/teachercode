package com.health.agent.module.chat.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.health.agent.common.exception.BusinessException;
import com.health.agent.module.ai.client.AIClient;
import com.health.agent.module.ai.dto.AIRequestDTO;
import com.health.agent.module.ai.dto.AIResponseDTO;
import com.health.agent.module.chat.entity.ChatMessage;
import com.health.agent.module.chat.entity.ChatSession;
import com.health.agent.module.chat.mapper.ChatMessageMapper;
import com.health.agent.module.chat.mapper.ChatSessionMapper;
import com.health.agent.module.chat.service.IChatService;
import com.health.agent.module.chat.vo.ChatMessageVO;
import com.health.agent.module.chat.vo.ChatSessionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天服务实现
 * 
 * @author Health Agent Team
 * @date 2025-01-01
 */
@Slf4j
@Service
public class ChatServiceImpl implements IChatService {
    
    @Autowired
    private ChatSessionMapper sessionMapper;
    
    @Autowired
    private ChatMessageMapper messageMapper;
    
    @Autowired
    private AIClient aiClient;
    
    @Value("${ai.model}")
    private String defaultModel;
    
    /**
     * 系统Prompt（后续可以从配置或数据库读取）
     */
    private static final String SYSTEM_PROMPT = 
        "你是'心理陪伴'，一个专业的心理健康AI助手，专门为大学生提供心理支持。\n" +
        "你的核心能力：\n" +
        "1. 共情倾听：理解用户的情绪和感受\n" +
        "2. SFBT疗法：使用焦点解决短期治疗技术引导用户\n" +
        "3. 积极关注：发现用户的优势和资源\n\n" +
        "你的对话原则：\n" +
        "1. 保持温暖、真诚、非评判的态度\n" +
        "2. 使用开放式问题引导用户思考\n" +
        "3. 关注用户的目标和解决方案\n" +
        "4. 发现例外经验（过去成功的经历）\n" +
        "5. 帮助用户制定可行的行动计划\n\n" +
        "重要提醒：\n" +
        "- 不要提供医学诊断\n" +
        "- 遇到危机情况要建议寻求专业帮助\n" +
        "- 保护用户隐私和信息安全";
    
    /**
     * AI对话上下文消息数量
     */
    private static final int CONTEXT_MESSAGE_COUNT = 10;
    
    @Override
    @Transactional
    public ChatSessionVO createSession(Long userId, String title) {
        log.info("创建会话，userId: {}, title: {}", userId, title);
        
        // 如果标题为空，使用默认标题
        if (StrUtil.isBlank(title)) {
            title = "新的对话";
        }
        
        // 创建会话
        ChatSession session = ChatSession.builder()
                .userId(userId)
                .title(title)
                .type("chat")
                .status(1)
                .lastMessageTime(LocalDateTime.now())
                .build();
        
        int result = sessionMapper.insert(session);
        if (result <= 0) {
            throw new BusinessException("创建会话失败");
        }
        
        log.info("会话创建成功，sessionId: {}", session.getId());
        
        // 转换为VO
        return convertSessionToVO(session);
    }
    
    @Override
    @Transactional
    public ChatMessageVO sendMessage(Long sessionId, String content) {
        log.info("发送消息，sessionId: {}, content: {}", sessionId, content);
        
        // 1. 验证会话是否存在
        ChatSession session = sessionMapper.findById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        
        // 2. 保存用户消息
        ChatMessage userMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .role("user")
                .content(content)
                .build();
        messageMapper.insert(userMessage);
        
        try {
            // 3. 构建AI请求（包含历史上下文）
            AIRequestDTO aiRequest = buildAIRequest(sessionId, content);
            
            // 4. 调用AI获取回复
            AIResponseDTO aiResponse = aiClient.chat(aiRequest);
            
            if (!aiResponse.getSuccess()) {
                throw new BusinessException("AI调用失败: " + aiResponse.getErrorMessage());
            }
            
            // 5. 保存AI回复
            ChatMessage aiMessage = ChatMessage.builder()
                    .sessionId(sessionId)
                    .role("assistant")
                    .content(aiResponse.getContent())
                    .tokens(aiResponse.getUsage() != null ? aiResponse.getUsage().getTotalTokens() : null)
                    .model(aiResponse.getModel())
                    .build();
            messageMapper.insert(aiMessage);
            
            // 6. 更新会话最后消息时间
            session.setLastMessageTime(LocalDateTime.now());
            sessionMapper.update(session);
            
            log.info("消息发送成功，返回AI回复");
            
            // 7. 返回AI消息
            return convertMessageToVO(aiMessage);
            
        } catch (Exception e) {
            log.error("发送消息失败", e);
            throw new BusinessException("发送消息失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<ChatSessionVO> getUserSessions(Long userId, int page, int size) {
        log.info("获取用户会话列表，userId: {}, page: {}, size: {}", userId, page, size);
        
        int offset = (page - 1) * size;
        List<ChatSession> sessions = sessionMapper.findByUserId(userId, offset, size);
        
        return sessions.stream()
                .map(this::convertSessionToVO)
                .collect(Collectors.toList());
    }
    
    @Override
    public ChatSessionVO getSessionById(Long sessionId) {
        log.info("获取会话详情，sessionId: {}", sessionId);
        
        ChatSession session = sessionMapper.findById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        
        return convertSessionToVO(session);
    }
    
    @Override
    public List<ChatMessageVO> getMessages(Long sessionId, int page, int size) {
        log.info("获取消息历史，sessionId: {}, page: {}, size: {}", sessionId, page, size);
        
        // 验证会话是否存在
        ChatSession session = sessionMapper.findById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        
        int offset = (page - 1) * size;
        List<ChatMessage> messages = messageMapper.findBySessionId(sessionId, offset, size);
        
        return messages.stream()
                .map(this::convertMessageToVO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public boolean deleteSession(Long sessionId) {
        log.info("删除会话，sessionId: {}", sessionId);
        
        // 验证会话是否存在
        ChatSession session = sessionMapper.findById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        
        // 删除会话（软删除）
        int result = sessionMapper.deleteById(sessionId);
        
        // 删除会话下的所有消息
        if (result > 0) {
            messageMapper.deleteBySessionId(sessionId);
        }
        
        return result > 0;
    }
    
    @Override
    public Integer countUserSessions(Long userId) {
        return sessionMapper.countByUserId(userId);
    }
    
    @Override
    public Integer countSessionMessages(Long sessionId) {
        return messageMapper.countBySessionId(sessionId);
    }
    
    // ================= 私有方法 =================
    
    /**
     * 构建AI请求（包含历史上下文）
     */
    private AIRequestDTO buildAIRequest(Long sessionId, String currentMessage) {
        AIRequestDTO request = AIRequestDTO.builder()
                .model(defaultModel)
                .sessionId(sessionId)
                .build();
        
        // 添加系统Prompt
        request.addSystemMessage(SYSTEM_PROMPT);
        
        // 获取最近的N条消息作为上下文
        List<ChatMessage> historyMessages = messageMapper.findLatestBySessionId(
                sessionId, CONTEXT_MESSAGE_COUNT);
        
        // 倒序添加（从旧到新）
        for (int i = historyMessages.size() - 1; i >= 0; i--) {
            ChatMessage msg = historyMessages.get(i);
            if ("user".equals(msg.getRole())) {
                request.addUserMessage(msg.getContent());
            } else if ("assistant".equals(msg.getRole())) {
                request.addAssistantMessage(msg.getContent());
            }
        }
        
        // 添加当前消息
        request.addUserMessage(currentMessage);
        
        return request;
    }
    
    /**
     * 转换会话为VO
     */
    private ChatSessionVO convertSessionToVO(ChatSession session) {
        ChatSessionVO vo = BeanUtil.copyProperties(session, ChatSessionVO.class);
        
        // 查询消息数量
        Integer messageCount = messageMapper.countBySessionId(session.getId());
        vo.setMessageCount(messageCount);
        
        return vo;
    }
    
    /**
     * 转换消息为VO
     */
    private ChatMessageVO convertMessageToVO(ChatMessage message) {
        return BeanUtil.copyProperties(message, ChatMessageVO.class);
    }
}

