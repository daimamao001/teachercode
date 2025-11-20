package com.health.agent.module.ai.controller;

import com.health.agent.common.api.ApiResponse;
import com.health.agent.module.ai.client.AIClient;
import com.health.agent.module.ai.dto.AIRequestDTO;
import com.health.agent.module.ai.dto.AIResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * AI测试控制器
 * 用于测试AI服务配置是否正确
 */
@Slf4j
@RestController
@RequestMapping("/api/ai/test")
@Tag(name = "AI测试接口", description = "用于测试AI服务配置（原生HTTP客户端）")
public class AITestController {

    @Autowired
    private AIClient aiClient;

    @Autowired(required = false)
    private com.health.agent.config.AIConfig aiConfig;

    /**
     * 测试AI对话接口（单轮）
     */
    @PostMapping("/chat")
    @Operation(summary = "测试AI对话", description = "发送一条消息给AI并获取回复")
    public ApiResponse<AIResponseDTO> testChat(@Validated @RequestBody TestChatRequest request) {
        log.info("测试AI对话，消息: {}", request.getMessage());
        try {
            AIRequestDTO aiRequest = AIRequestDTO.builder()
                    .build()
                    .addSystemMessage("你是一个友好的AI助手，请用简洁的语言回答用户的问题。")
                    .addUserMessage(request.getMessage());

            if (request.getTemperature() != null) {
                aiRequest.setTemperature(request.getTemperature());
            }
            if (request.getMaxTokens() != null) {
                aiRequest.setMaxTokens(request.getMaxTokens());
            }

            AIResponseDTO response = aiClient.chat(aiRequest);
            if (Boolean.TRUE.equals(response.getSuccess())) {
                log.info("AI对话测试成功");
                return ApiResponse.ok(response);
            } else {
                log.error("AI对话测试失败: {}", response.getErrorMessage());
                return ApiResponse.fail("AI调用失败: " + response.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("AI对话测试异常", e);
            return ApiResponse.fail("测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试多轮对话
     */
    @PostMapping("/multi-chat")
    @Operation(summary = "测试多轮对话", description = "测试AI的上下文理解能力")
    public ApiResponse<AIResponseDTO> testMultiChat(@Validated @RequestBody MultiChatRequest request) {
        log.info("测试多轮对话，消息数: {}", request.getMessages() == null ? 0 : request.getMessages().size());
        try {
            AIRequestDTO aiRequest = AIRequestDTO.builder()
                    .build()
                    .addSystemMessage("你是一个友好的AI助手。");

            if (request.getMessages() != null) {
                for (ChatMessage msg : request.getMessages()) {
                    if ("user".equals(msg.getRole())) {
                        aiRequest.addUserMessage(msg.getContent());
                    } else if ("assistant".equals(msg.getRole())) {
                        aiRequest.addAssistantMessage(msg.getContent());
                    }
                }
            }

            AIResponseDTO response = aiClient.chat(aiRequest);
            if (Boolean.TRUE.equals(response.getSuccess())) {
                log.info("多轮对话测试成功");
                return ApiResponse.ok(response);
            } else {
                log.error("多轮对话测试失败: {}", response.getErrorMessage());
                return ApiResponse.fail("AI调用失败: " + response.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("多轮对话测试异常", e);
            return ApiResponse.fail("测试失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "AI服务健康检查", description = "检查AI服务是否可用")
    public ApiResponse<HealthStatus> healthCheck() {
        try {
            AIRequestDTO request = AIRequestDTO.builder()
                    .build()
                    .addSystemMessage("你是一个AI助手。")
                    .addUserMessage("你好");

            long startTime = System.currentTimeMillis();
            AIResponseDTO response = aiClient.chat(request);
            long responseTime = System.currentTimeMillis() - startTime;

            HealthStatus status = new HealthStatus();
            status.setStatus(Boolean.TRUE.equals(response.getSuccess()) ? "healthy" : "unhealthy");
            status.setResponseTime(responseTime);
            status.setMessage(Boolean.TRUE.equals(response.getSuccess()) ? "AI服务正常（原生HTTP客户端）" : response.getErrorMessage());

            if (aiConfig != null) {
                status.setProvider(aiConfig.getProvider());
                status.setModel(aiConfig.getModel());
                status.setApiUrl(aiConfig.getApiUrl());
            }

            if (response.getUsage() != null) {
                status.setTokensUsed(response.getUsage().getTotalTokens());
            }

            return ApiResponse.ok(status);
        } catch (Exception e) {
            log.error("健康检查失败", e);
            HealthStatus status = new HealthStatus();
            status.setStatus("error");
            status.setMessage("健康检查失败: " + e.getMessage());
            return ApiResponse.ok(status);
        }
    }

    // ================= 请求对象 =================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestChatRequest {
        @NotBlank(message = "消息不能为空")
        @Parameter(description = "测试消息内容")
        private String message;

        @Parameter(description = "温度参数(0.0-1.0)，控制回复的随机性")
        private Double temperature;

        @Parameter(description = "最大生成token")
        private Integer maxTokens;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MultiChatRequest {
        @Parameter(description = "对话消息列表")
        private java.util.List<ChatMessage> messages;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {
        @Parameter(description = "角色：user或assistant")
        private String role;

        @Parameter(description = "消息内容")
        private String content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HealthStatus {
        @Parameter(description = "状态：healthy/unhealthy/error")
        private String status;

        @Parameter(description = "响应时间（毫秒）")
        private Long responseTime;

        @Parameter(description = "状态信息")
        private String message;

        @Parameter(description = "使用的token")
        private Integer tokensUsed;

        @Parameter(description = "AI服务提供商")
        private String provider;

        @Parameter(description = "模型名称")
        private String model;

        @Parameter(description = "API地址")
        private String apiUrl;
    }
}