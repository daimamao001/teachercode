package com.health.agent.module.chat.controller;

import com.health.agent.common.api.ApiResponse;
import com.health.agent.common.util.JwtUtil;
import com.health.agent.module.chat.dto.CreateSessionDTO;
import com.health.agent.module.chat.dto.SendMessageDTO;
import com.health.agent.module.chat.service.IChatService;
import com.health.agent.module.chat.vo.ChatMessageVO;
import com.health.agent.module.chat.vo.ChatSessionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天控制器
 * 
 * @author Health Agent Team
 * @date 2025-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@Tag(name = "聊天管理", description = "聊天会话和消息管理接口")
public class ChatController {
    
    @Autowired
    private IChatService chatService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 创建会话
     */
    @PostMapping("/sessions")
    @Operation(summary = "创建会话", description = "创建新的聊天会话")
    public ApiResponse<ChatSessionVO> createSession(
            @Validated @RequestBody CreateSessionDTO dto) {
        
        // TODO: 从JWT获取当前用户ID
        Long userId = getCurrentUserId();
        
        log.info("创建会话，userId: {}, title: {}", userId, dto.getTitle());
        
        ChatSessionVO session = chatService.createSession(userId, dto.getTitle());
        return ApiResponse.ok(session);
    }
    
    /**
     * 获取用户会话列表
     */
    @GetMapping("/sessions")
    @Operation(summary = "获取会话列表", description = "分页获取用户的会话列表")
    public ApiResponse<List<ChatSessionVO>> getSessions(
            @Parameter(description = "页码", example = "1") 
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量", example = "20") 
            @RequestParam(defaultValue = "20") int size) {
        
        Long userId = getCurrentUserId();
        
        log.info("获取会话列表，userId: {}, page: {}, size: {}", userId, page, size);
        
        List<ChatSessionVO> sessions = chatService.getUserSessions(userId, page, size);
        return ApiResponse.ok(sessions);
    }
    
    /**
     * 获取会话详情
     */
    @GetMapping("/sessions/{id}")
    @Operation(summary = "获取会话详情", description = "根据ID获取会话详情")
    public ApiResponse<ChatSessionVO> getSessionDetail(
            @Parameter(description = "会话ID", required = true) 
            @PathVariable Long id) {
        
        log.info("获取会话详情，sessionId: {}", id);
        
        ChatSessionVO session = chatService.getSessionById(id);
        return ApiResponse.ok(session);
    }
    
    /**
     * 删除会话
     */
    @DeleteMapping("/sessions/{id}")
    @Operation(summary = "删除会话", description = "删除指定的会话（包括所有消息）")
    public ApiResponse<Boolean> deleteSession(
            @Parameter(description = "会话ID", required = true) 
            @PathVariable Long id) {
        
        log.info("删除会话，sessionId: {}", id);
        
        boolean result = chatService.deleteSession(id);
        return ApiResponse.ok(result);
    }
    
    /**
     * 发送消息
     */
    @PostMapping("/messages")
    @Operation(summary = "发送消息", description = "发送消息并获取AI回复")
    public ApiResponse<ChatMessageVO> sendMessage(
            @Validated @RequestBody SendMessageDTO dto) {
        
        log.info("发送消息，sessionId: {}, content: {}", dto.getSessionId(), dto.getContent());
        
        ChatMessageVO message = chatService.sendMessage(dto.getSessionId(), dto.getContent());
        return ApiResponse.ok(message);
    }
    
    /**
     * 获取消息历史
     */
    @GetMapping("/messages")
    @Operation(summary = "获取消息历史", description = "分页获取会话的消息历史")
    public ApiResponse<List<ChatMessageVO>> getMessages(
            @Parameter(description = "会话ID", required = true) 
            @RequestParam Long sessionId,
            @Parameter(description = "页码", example = "1") 
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量", example = "20") 
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("获取消息历史，sessionId: {}, page: {}, size: {}", sessionId, page, size);
        
        List<ChatMessageVO> messages = chatService.getMessages(sessionId, page, size);
        return ApiResponse.ok(messages);
    }
    
    // ================= 私有方法 =================
    
    /**
     * 获取当前登录用户ID
     * TODO: 实际应从JWT Token或Spring Security Context中获取
     */
    private Long getCurrentUserId() {
        // 临时返回测试用户ID
        // 实际使用时需要从请求头中解析JWT Token
        Long userId = jwtUtil.getCurrentUserId();
        if (userId == null) {
            // 开发阶段返回默认用户ID
            log.warn("未获取到用户ID，使用默认值1");
            return 1L;
        }
        return userId;
    }
}

